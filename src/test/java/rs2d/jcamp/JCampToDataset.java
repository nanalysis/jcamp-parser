package rs2d.jcamp;

import static rs2d.jcamp.util.JCampUtil.toNucleusName;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;

import lombok.SneakyThrows;
import rs2d.commons.io.IOUtil;
import rs2d.commons.log.Log;
import rs2d.commons.misc.ArrayUtil;
import rs2d.commons.xml.XmlSerializationException;
import rs2d.jcamp.model.JCampBlock;
import rs2d.jcamp.model.JCampDocument;
import rs2d.jcamp.model.JCampPage;
import rs2d.jcamp.model.Label;
import rs2d.jcamp.parser.JCampParser;
import rs2d.spinlab.application.Application;
import rs2d.spinlab.data.Data;
import rs2d.spinlab.data.DataSet;
import rs2d.spinlab.data.Header;
import rs2d.spinlab.data.io.DatasetIO;
import rs2d.spinlab.study.Serie;
import rs2d.spinlab.study.SerieIO;
import rs2d.spinlab.tools.param.*;
import rs2d.spinlab.tools.utility.Step;

public class JCampToDataset {
    private void convertFullDocument(JCampDocument doc, File outputDir) throws IOException {
        createEmptySerie(doc, outputDir);

        JCampBlock fidBlock = doc.blocks().filter(block -> block.getDataType().isFID()).findFirst().orElse(null);
        if (fidBlock != null) {
            DataSet fid = jcampToDataset(fidBlock);
            DatasetIO.saveDataset(fid, outputDir);
        }

        List<JCampBlock> spectra = doc.blocks().filter(block -> block.getDataType().isSpectrum()).collect(Collectors.toList());
        for (JCampBlock spectrum : spectra) {
            DataSet proc = jcampToDataset(spectrum);
            File procDir = DatasetIO.saveProcess(proc, outputDir);
            createEmptySerie(doc, procDir);
        }
    }

    private DataSet jcampToDataset(JCampBlock block) {
        DataSet dataSet = newEmptyDataSetForBlock(block);
        fillHeader(dataSet.getHeader(), block);
        Data receiver = dataSet.getData(0);

        List<JCampPage> realPages = block.getPagesForYSymbol("R");
        List<JCampPage> imaginaryPages = block.getPagesForYSymbol("I");
        if (realPages.isEmpty()) {
            // XYDATA or Bruker 2D uses this
            // for example in Bruker 2D, data tables are declared with (F2++(Y..Y))
            realPages = block.getPagesForYSymbol("Y");
        }

        fillDataFromPages(receiver, realPages, imaginaryPages);
        return dataSet;
    }

    private void fillDataFromPages(Data data, List<JCampPage> realPages, List<JCampPage> imaginaryPages) {
        float[][] real = data.getRealPart()[0][0];
        if (realPages.size() != real.length) {
            throw new IllegalStateException(
                "Number of real data pages isn't what's expected. Expected: " + real.length + ", actual: " + realPages.size());
        }
        fillDataFromPages(real, realPages);

        if (!imaginaryPages.isEmpty()) {
            float[][] imaginary = data.getImaginaryPart()[0][0];
            if (imaginaryPages.size() != imaginary.length) {
                throw new IllegalStateException(
                    "Number of imaginary data pages isn't what's expected. Expected: " + imaginary.length + ", actual: " + imaginaryPages.size());
            }

            fillDataFromPages(imaginary, imaginaryPages);
        }
    }

    private void fillDataFromPages(float[][] matrix, List<JCampPage> pages) {
        pages.sort(Comparator.comparing(JCampPage::extractPageValueAsNumber));

        for (int i = 0; i < pages.size(); i++) {
            JCampPage page = pages.get(i);
            try {
                float[] points = ArrayUtil.doubleToFloatArray(page.toArray());
                if (points.length != matrix[0].length) {
                    Log.error(getClass(), "Number of points isn't what's expected. Expected: %d, actual: %d", matrix[0].length, points.length);
                    points = Arrays.copyOf(points, matrix[0].length);
                }
                matrix[i] = points;
            } catch (RuntimeException e) {
                Log.error(getClass(), "Parse error on data table starting at line: %d", page.get(Label.DATA_TABLE).getLineNumber());
                throw e;
            }
        }
    }

    private DataSet newEmptyDataSetForBlock(JCampBlock block) {
        int size1d;
        int size2d;

        if (block.contains(Label.VAR_DIM)) {
            int numDim = block.getOrDefault(Label.NUMDIM, "1").getInt();
            int[] dimensions = block.getOrDefault(Label.VAR_DIM, "").getInts();
            size1d = dimensions[1];
            size2d = numDim > 1 ? dimensions[0] : 1; // only use VAR_DIM[0] for multi-dimensional data
        } else {
            size1d = block.get(Label.NPOINTS).getInt();
            size2d = 1;
        }

        return new DataSet(size1d, size2d, 1, 1, 1, ModalityEnum.NMR);
    }

    private void fillHeader(Header header, JCampBlock block) {
        // NOTE: tags from jcamp export with topspin templates: BASIC_TAGS, HETERO_1D_TAGS
        // TODO check nanalysis data to see what parameter they store, use these first
        // ignored records
        // ##.ACQUISITION MODE= {% if header.ACQUISITION_MODE[0]=='COMPLEX' %} SIMULTANEOUS {% else %} REAL {% endif %}
        // ##.DIGITISER RES= {{ params.digitalresolution }}
        // ##$DIGMOD= {% if header.DIGITAL_FILTER_REMOVED=='true' %} 0 {% else %} 1 {% endif %}
        // ##$DSPFVS= {% if header.DIGITAL_FILTER_REMOVED=='true' %} -1 {% else %} 24 {% endif %}
        // ##$SI= {{data.size1}}
        // ##$TD= {{ 2*int(header.ACQUISITION_MATRIX_DIMENSION_1D) }}
        // ##$YMAX_a= {{data.buffer.max()}}
        // ##$YMIN_a= {{data.buffer.min()}}
        // ##$YMAX_p= 0
        // ##$YMIN_p= 0
        // ##$STSI= 0
        // ##$STSR= 0
        // ##$TDeff= 0
        // ##.SHIFT REFERENCE= INTERNAL, {{ header.SOLVENT }}, 1, {{ 1000000*(float(header.SPECTRAL_WIDTH)/2 +
        // float(header.OBSERVED_OFFSET_FREQ))/float(header.OBSERVED_FREQUENCY) }}
        // ##$OFFSET= {{ 1000000*(float(header.SPECTRAL_WIDTH)/2 + float(header.OBSERVED_OFFSET_FREQ))/float(header.OBSERVED_FREQUENCY) }}
        // ##$SFO2= {{ (float(header.NON_OBSERVED_BASE_FREQ)+float(header.NON_OBSERVED_OFFSET_FREQ))/1000000 }}

        block.optional(Label.ORIGIN).ifPresent(record -> header.getTextParam(DefaultParams.MANUFACTURER).setValue(record.getString()));
        block.optional(Label.OWNER)
            .ifPresent(record -> header.putParam(new TextParam(record.getNormalizedLabel(), record.getString(), record.getComment())));
        block.optional(Label._DELAY).ifPresent(record -> header.putParam(new ListNumberParam("DE",
            Arrays.stream(record.getDoubles()).map(d -> d / 1e6).boxed().collect(Collectors.toList()),
            NumberEnum.Double, record.getComment())));
        block.optional(Label._AVERAGES).ifPresent(record -> header.getNumberParam(DefaultParams.NUMBER_OF_AVERAGES).setValue(record.getInt()));

        // TODO PRE_SCAN should be a default param
        block.optional(Label.$DS)
            .ifPresent(record -> header.putParam(new NumberParam("Pre_scan", record.getInt(), NumberEnum.Integer, record.getComment())));
        // TODO should we also set DIGITAL_FILTER_REMOVED for $GRPDLY?
        block.optional(Label.$GRPDLY).ifPresent(record -> header.getNumberParam(DefaultParams.DIGITAL_FILTER_SHIFT).setValue(record.getInt()));
        block.optional(Label.$NS).ifPresent(record -> header.getNumberParam(DefaultParams.NUMBER_OF_AVERAGES).setValue(record.getInt()));
        block.optional(Label.$PHC0).ifPresent(record -> header.getListNumberParam(DefaultParams.PHASE_0).setValueAt(0, record.getDouble()));
        block.optional(Label.$PHC1).ifPresent(record -> header.getListNumberParam(DefaultParams.PHASE_1).setValueAt(0, record.getDouble()));
        // TODO should we try to use this to deduce observed freq?
        // ##$SW= {{ float(header.SPECTRAL_WIDTH)*1000000/float(header.OBSERVED_FREQUENCY) }}
        block.optional(Label.SPECTRAL_WIDTH, Label._SPECTRAL_WIDTH, Label.$SW_H)
            .ifPresent(record -> header.getNumberParam(DefaultParams.SPECTRAL_WIDTH).setValue(record.getDouble()));
        block.optional(Label.$RG).ifPresent(record -> header.getListNumberParam(DefaultParams.RECEIVER_GAIN).setValueAt(0, record.getDouble()));


        // nuclei
        block.optional(Label._OBSERVE_NUCLEUS)
            .ifPresent(record -> header.getTextParam(DefaultParams.OBSERVED_NUCLEUS).setValue(toNucleusName(record.getString())));
        block.optional(Label.$NUC_1, Label._OBSERVE_NUCLEUS)
            .ifPresent(record -> header.getTextParam(DefaultParams.NUCLEUS_1).setValue(toNucleusName(record.getString())));
        block.optional(Label.$NUC_2).ifPresent(record -> header.getTextParam(DefaultParams.NUCLEUS_2).setValue(toNucleusName(record.getString())));

        // frequencies
        block.optional(Label._OBSERVE_FREQUENCY, Label.$SFO1)
            .ifPresent(record -> header.getNumberParam(DefaultParams.OBSERVED_FREQUENCY).setValue(record.getDouble() * 1e6));
        // TODO how to know if $BFREQ is supposed to be BASE_FREQ_1 or 2?
        block.optional(Label.$BF1, Label.$BFREQ, Label.$SF)
            .ifPresent(record -> header.getNumberParam(DefaultParams.BASE_FREQ_1).setValue(record.getDouble() * 1e6));
        block.optional(Label.$BF2).ifPresent(record -> header.getNumberParam(DefaultParams.BASE_FREQ_2).setValue(record.getDouble() * 1e6));
        block.optional(Label.$O1).ifPresent(record -> header.getNumberParam(DefaultParams.OFFSET_FREQ_1).setValue(record.getDouble()));
        block.optional(Label.$O2).ifPresent(record -> header.getNumberParam(DefaultParams.OFFSET_FREQ_2).setValue(record.getDouble()));

        // possible duplicates, select standard one first
        block.optional(Label.TEMPERATURE, Label.$TE).ifPresent(record -> {
            // tries to guess whether stored in celcius (<=150) or in Kelvin (>150)
            double temperatureKelvin = record.getDouble() > 150 ? record.getDouble() : 273.15 + record.getDouble();
            header.getNumberParam(NmrDefaultParams.SAMPLE_TEMPERATURE).setValue(temperatureKelvin);
        });
        block.optional(Label._PULSE_SEQUENCE, Label.$PULPROG)
            .ifPresent(record -> header.getTextParam(DefaultParams.SEQUENCE_NAME).setValue(record.getString()));
        block.optional(Label._SOLVENT_NAME, Label.$SOLVENT)
            .ifPresent(record -> header.getTextParam(NmrDefaultParams.SOLVENT).setValue(record.getString()));

        // other specific ones
        if (block.getDataType().isSpectrum()) {
            ListNumberParam state = (ListNumberParam) new DefaultParams().getParam(DefaultParams.STATE);
            header.putParam(state);

            state.setValueAt(0, DataState.SPECTRUM.getValue());
            if (header.getNumberParam(NmrDefaultParams.MATRIX_DIMENSION_2D).getValue().intValue() > 1) {
                state.setValueAt(1, DataState.SPECTRUM.getValue());
            }
        }
    }

    private void createEmptySerie(JCampDocument doc, File outputDir) throws XmlSerializationException {
        // TODO fill serie form jcamp data
        // ##TITLE= #{{ file.id }} - {{file.name}} - {% if file.user %} {{file.user}} {% else %} anonymous {% endif %}
        // ##JCAMPDX= {{ params.jcamp_signature }}
        // ##DATE= {{ file.date }}
        // ##LONG DATE= {{file.acquisitionTime }}
        // ##$DATAPATH= {{ file.fullpathname }}
        // ##$NAME= <{{ file.pathname }}>
        // ##$DATE= {{ int(file.epoch_date) }}
        // ##$EXPNO= 1
        // ##$PROCNO= 1

        // an application is needed to allow processing, otherwise it can't modify a processing queue
        Application application = new Application();
        application.setName(doc.getTitle());

        Serie serie = new Serie(application);
        serie.setCurrentStep(Step.ExportDone);
        serie.setOutputDirectory(outputDir);
        // TODO guess default process list
        SerieIO.saveXml(serie);
    }

    @Test
    @Ignore("manual test")
    public void benchtop60() throws IOException {
        String resourcePath = "/benchtop/60/";
        File outputRootDir = new File("dataset/nanalysis-60");

        List<String> files = List.of(
            "NMReady_1D_1H_20210909_Test_formates.dx",
            "NMReady_1D_1H_20210909_Test_formatesS.jdx",
            "NMReady_COSY_1H_20210909_test.dx",
            "NMReady_COSY_1H_20210909_test_ref_f1.dx",
            "NMReady_COSY_1H_20210909_test_ref_f1S.jdx",
            "NMReady_COSY_1H_20210909_test_ref_f2.dx",
            "NMReady_COSY_1H_20210909_test_ref_f2S.jdx",
            "X560_COSY_1H_20210611_S351_PLH-158-25B_256s512p_16.dx");

        for (String file : files) {
            String shortName = file.split("\\.")[0];
            File outputDir = new File(outputRootDir, shortName);
            JCampDocument doc = new JCampParser().parse(resourceAsString(resourcePath + file));
            convertFullDocument(doc, outputDir);
        }
    }

    @Test
    @Ignore("manual test")
    public void benchtop100() throws IOException {
        String resourcePath = "/benchtop/100/";
        File outputRootDir = new File("dataset/nanalysis-100");

        List<String> files = List.of(
            "NMReady_1D_1H_20210302_quinine_4.dx",
            "NMReady_1D_1H_20210316_DEP_4-2.dx",
            "NMReady_1D_1H_20210705_000_TH_1H.dx",
            "NMReady_COSY_1H_20210324_dep_64x512.dx",
            "NMReady_COSY_1H_20210706_003_TH_1,3-butanediol-2M-D2O.dx",
            "NMReady_COSY_1H_20210706_003_TH_1,3-butanediol-2M-D2O_ref_f1.dx",
            "NMReady_COSY_1H_20210706_003_TH_1,3-butanediol-2M-D2O_ref_f2.dx",
            "NMReady_T1_1H_20210722_004_TH_red_tube_quadratic_with integral.dx");

        for (String file : files) {
            String shortName = file.split("\\.")[0];
            File outputDir = new File(outputRootDir, shortName);
            JCampDocument doc = new JCampParser().parse(resourceAsString(resourcePath + file));
            convertFullDocument(doc, outputDir);
        }
    }

    @Test
    @Ignore("manual test")
    public void bothFromOldSpinitExport() throws IOException {
        JCampDocument doc = new JCampParser().parse(resourceAsString("/spinit/oldexport/PulseAcquisition.dx"));
        File outputDir = new File("dataset/PulseAcquisition");
        convertFullDocument(doc, outputDir);
    }

    @Test
    @Ignore("manual test")
    public void bothFromSpinitCascadeExport() throws IOException {
        String resourcePath = "/spinit/cascade/";
        File outputRootDir = new File("dataset/cascade");

        List<String> files = List.of(
            "HETERO2D_REF.dx",
            "demo_HSQC_ET_GS_GARP-4_4_0.dx");

        for (String file : files) {
            String shortName = file.split("\\.")[0];
            File outputDir = new File(outputRootDir, shortName);
            JCampDocument doc = new JCampParser().parse(resourceAsString(resourcePath + file));
            convertFullDocument(doc, outputDir);
        }
    }

    @Test
    @Ignore("manual test")
    public void allMagritek() throws IOException {
        String resourcePath = "/magritek/";
        File outputRootDir = new File("dataset/magritek");

        List<String> files = List.of(
            "43-ABR-2-spectra.jdx", "43-CHCl3-1-spectra.jdx", "43MHz-1-raw.jdx", "43MHz-2-raw.jdx",
            "60-ABR-1-spectra.jdx", "60-CHCl3-1.jdx", "60MHz-1-raw.jdx", "60MHz-2-raw.jdx");

        for (String file : files) {
            String shortName = file.split("\\.")[0];
            File outputDir = new File(outputRootDir, shortName);
            JCampDocument doc = new JCampParser().parse(resourceAsString(resourcePath + file));
            convertFullDocument(doc, outputDir);
        }
    }

    @SneakyThrows
    private String resourceAsString(String name) {
        // TODO change this path to test
        // data available on \\baleine\Logiciel\temp\jcamp-test-resources
        File base = new File("C:\\workspace\\data\\jcamp\\test");

        try (var input = new FileInputStream(new File(base, name))) {
            return IOUtil.readAsString(input);
        }
    }
}
