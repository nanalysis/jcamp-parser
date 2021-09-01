package rs2d.jcamp.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import lombok.SneakyThrows;
import rs2d.commons.io.IOUtil;
import rs2d.jcamp.model.DataClass;
import rs2d.jcamp.model.DataType;
import rs2d.jcamp.model.JCampDocument;

public class JCampParserTest {
    @Test
    public void linkDataTypeWithSeveralBlocks() {
        JCampDocument data = new JCampParser().parse(resourceAsString("/spinit/cascade/Cyclosporin_TOCSY_73_0.dx"));
        assertEquals("#73 - TOCSY -  Gaetan", data.getTitle());
        assertEquals("6.0", data.getVersion());
        assertEquals(DataType.LINK, data.getDataType());
        assertEquals(2, data.getBlockCount());
        assertTrue(data.containsDeclaredNumberOfBlocks());
        assertEquals(DataType.ND_NMR_FID, data.block(0).getDataType());
        assertEquals(DataType.ND_NMR_SPECTRUM, data.block(1).getDataType());
    }

    @Test
    public void fidDataType1D() {
        JCampDocument data = new JCampParser().parse(resourceAsString("/benchtop/100/NMReady_1D_1H_20210302_quinine_4.dx"));
        assertEquals("NMReady_1D_1H_20210302_quinine_4", data.getTitle());
        assertEquals("5.01", data.getVersion());
        assertEquals(DataType.NMR_FID, data.getDataType());
        assertEquals(1, data.getBlockCount());
        assertTrue(data.containsDeclaredNumberOfBlocks());
        assertEquals(DataType.NMR_FID, data.block(0).getDataType());
        assertEquals(DataClass.NTUPLES, data.block(0).getDataClass());
    }

    @Test
    public void fidDataType2D() {
        JCampDocument data = new JCampParser().parse(resourceAsString("/benchtop/100/NMReady_COSY_1H_20210324_dep_64x512.dx"));
        assertEquals("NMReady_COSY_1H_20210324_dep_64x512", data.getTitle());
        assertEquals("6.0", data.getVersion());
        assertEquals(DataType.ND_NMR_FID, data.getDataType());
        assertEquals(1, data.getBlockCount());
        assertTrue(data.containsDeclaredNumberOfBlocks());
        assertEquals(DataType.ND_NMR_FID, data.block(0).getDataType());
        assertEquals(DataClass.NTUPLES, data.block(0).getDataClass());
    }

    @Test
    public void spectrumDataType1D() {
        JCampDocument data = new JCampParser().parse(resourceAsString(
            "/benchtop/60/NMReady_1D_1H_20210909_Test_formatesS.jdx"));
        assertEquals("NMReady_1D_1H_20210909_Test_formates", data.getTitle());
        assertEquals("5.01", data.getVersion());
        assertEquals(DataType.NMR_SPECTRUM, data.getDataType());
        assertEquals(1, data.getBlockCount());
        assertTrue(data.containsDeclaredNumberOfBlocks());
        assertEquals(DataType.NMR_SPECTRUM, data.block(0).getDataType());
        assertEquals(DataClass.XYDATA, data.block(0).getDataClass());
    }

    @Test
    public void checkBlockInformation() {
        JCampDocument data = new JCampParser().parse(resourceAsString("/spinit/cascade/Cyclosporin_TOCSY_73_0.dx"));
        assertEquals(2, data.getBlockCount());
        assertTrue(data.containsDeclaredNumberOfBlocks());
        assertEquals("#73 - TOCSY -  Gaetan", data.block(0).getTitle());
        assertEquals("6.0", data.block(0).getVersion());
        assertEquals(DataType.ND_NMR_FID, data.block(0).getDataType());
        assertEquals(DataClass.NTUPLES, data.block(0).getDataClass());
        assertEquals("#73 - TOCSY -  Gaetan", data.block(1).getTitle());
        assertEquals("6.0", data.block(1).getVersion());
        assertEquals(DataType.ND_NMR_SPECTRUM, data.block(1).getDataType());
        assertEquals(DataClass.NTUPLES, data.block(1).getDataClass());
    }

    @Test
    public void checkRecordDimension() {
        JCampDocument data = new JCampParser().parse(resourceAsString("/benchtop/100/NMReady_COSY_1H_20210324_dep_64x512.dx"));
        assertEquals(1, data.getBlockCount());
        assertTrue(data.containsDeclaredNumberOfBlocks());
        assertEquals(512, data.block(0).get("$SI", 0).getInt());
        assertEquals(64, data.block(0).get("$SI", 1).getInt());
    }

    @Test
    public void checkMultiplePages() {
        JCampDocument data = new JCampParser().parse(resourceAsString("/benchtop/100/NMReady_COSY_1H_20210706_003_TH_1,3-butanediol-2M-D2O.dx"));
        assertEquals(1, data.getBlockCount());
        assertTrue(data.containsDeclaredNumberOfBlocks());
        assertEquals(256, data.block(0).getPageCount());
    }

    @Test(expected = IllegalStateException.class)
    public void checkValueOutOfDocument() {
        new JCampParser().parse("##END=\n##FAILURE=document ended");
    }

    @SneakyThrows
    private String resourceAsString(String name) {
        try (var input = getClass().getResourceAsStream(name)) {
            return IOUtil.readAsString(input);
        }
    }
}
