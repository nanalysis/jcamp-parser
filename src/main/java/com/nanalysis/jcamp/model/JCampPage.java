package com.nanalysis.jcamp.model;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.nanalysis.jcamp.parser.ASDFParser;

/**
 * A JCamp data page. A page starts when the "PAGE" LDR is first seen, and end with a specific "END" tag.
 * It can contain several attributes, including the "DATA TABLE".
 */
public class JCampPage extends JCampContainer {
    protected final JCampContainer parent;

    public JCampPage(JCampContainer parent) {
        this.parent = parent;
    }

    /**
     * Get the page header, as defined by the "DATA TABLE" first line.
     * 
     * @return the header
     */
    public String getHeader() {
        return get(Label.DATA_TABLE).getString().lines().findFirst()
            .orElseThrow(() -> new IllegalStateException("Empty data header!"));
    }

    /**
     * Get the page content, as defined by the "DATA TABLE" lines, excluding the first line header.
     * 
     * @return a list of lines, without end-of-line characters.
     */
    public List<String> getDataLines() {
        return get(Label.DATA_TABLE).getString().lines().skip(1)
            .collect(Collectors.toList());
    }

    /**
     * Extract the symbol defining the page.
     * Ex: for "##PAGE=N=1", the symbol would be "N"
     * 
     * @return the symbol used in this page.
     */
    public String extractPageSymbol() {
        String pageValue = get(Label.PAGE).getString();
        return pageValue.split("=", 2)[0];
    }


    /**
     * Extract the page value.
     * Ex: for "##PAGE=N=1", the value would be "1"
     *
     * @return the value defined in this page.
     */
    public String extractPageValue() {
        String pageValue = get(Label.PAGE).getString();
        return pageValue.split("=", 2)[1];
    }

    /**
     * Extract the page value and parse it as a floating point number.
     * Ex: for "##PAGE=T2=0.09", the value would be 0.09
     *
     * @return the value defined in this page.
     */
    public double extractPageValueAsNumber() {
        return Double.parseDouble(extractPageValue());
    }

    /**
     * Extract X symbol from a DATA TABLE or XYDATA header.
     * This is the variable used for the X axis.
     * Ex: for "(T2++(R..R))", the X symbol would be "T2"
     *
     * @return extracted X symbol.
     */
    public String extractXSymbol() {
        return extractSymbols(getHeader())[0];
    }

    /**
     * Extract Y symbol from a DATA TABLE or XYDATA header.
     * This is the variable used for the Y axis.
     * Ex: for "(T2++(R..R))", the Y symbol would be "R"
     *
     * @return extracted Y symbol.
     */
    public String extractYSymbol() {
        return extractSymbols(getHeader())[1];
    }

    private int findSymbolIndex(String symbol) {
        List<String> symbols = parent.getOrDefault(Label.SYMBOL, "").getStrings();
        int index = symbols.indexOf(symbol);
        if (index < 0) {
            throw new IllegalArgumentException("Undefined symbol: " + symbol + ", known ones are: " + symbols);
        }
        return index;
    }

    private String getAttributeForSymbol(Label label, String symbol, String defaultValue) {
        int index = findSymbolIndex(symbol);
        List<String> forms = parent.getOrDefault(label, "").getStrings();
        if (index < forms.size()) {
            return forms.get(index);
        }
        return defaultValue;
    }

    /**
     * Search in which form is a specific data stored. Values include "AFFN" and "ASDF".
     *
     * @param symbol the symbol
     * @return the form used to store the data represented by this symbol.
     */
    protected Form getFormForSymbol(String symbol) {
        return Form.fromString(getAttributeForSymbol(Label.VAR_FORM, symbol, "AFFN"));
    }

    /**
     * Get the number of dimensions defined for a specific variable.
     *
     * @param symbol the variable symbol
     * @return the number of dimensions.
     */
    protected int getDimensionForSymbol(String symbol) {
        String dim = getAttributeForSymbol(Label.VAR_DIM, symbol, "1");
        return Integer.parseInt(dim);
    }

    /**
     * Get the scaling factor defined for a specific variable.
     *
     * @param symbol the variable symbol
     * @return the scaling factor.
     */
    protected double getFactorForSymbol(String symbol) {
        String factor = getAttributeForSymbol(Label.FACTOR, symbol, "1");
        return Double.parseDouble(factor);
    }

    /**
     * Get the first value defined for a specific variable.
     *
     * @param symbol the variable symbol
     * @return the first value.
     */
    protected double getFirstForSymbol(String symbol) {
        String first = getAttributeForSymbol(Label.FIRST, symbol, "0");
        return Double.parseDouble(first);
    }

    /**
     * Get the last value defined for a specific variable.
     *
     * @param symbol the variable symbol
     * @return the last value.
     */
    protected double getLastForSymbol(String symbol) {
        String last = getAttributeForSymbol(Label.LAST, symbol, "0");
        return Double.parseDouble(last);
    }

    /**
     * Read the page data content. Both AFFN and ASDF storage forms are supported.
     *
     * @return the page data.
     */
    public double[] toArray() {
        String[] symbols = extractSymbols(getHeader());
        String xSymbol = symbols[0];
        String ySymbol = symbols[1];

        int size = getDimensionForSymbol(xSymbol);

        Form form = getFormForSymbol(ySymbol);
        if (form == Form.AFFN) {
            return affnToArray(ySymbol, size);
        } else if (form == Form.ASDF) {
            return asdfToArray(ySymbol, size);
        } else {
            throw new IllegalArgumentException("Unsupported symbol form, only AFFN and ASDF are supported: " + form);
        }
    }

    private double[] asdfToArray(String ySymbol, int size) {
        double yFactor = getFactorForSymbol(ySymbol);

        int[] values = new ASDFParser(size).parse(getDataLines());
        return Arrays.stream(values).mapToDouble(i -> i * yFactor).toArray();
    }

    private double[] affnToArray(String ySymbol, int size) {
        double[] array = new double[size];
        List<String> lines = getDataLines();

        double factor = getFactorForSymbol(ySymbol);

        int index = 0;
        for (String line : lines) {
            String[] values = line.split("\\s+");

            // first value is X corresponding to first Y value, others are Y values
            // ignore it for now, assume all lines are in order
            for (int i = 1; i < values.length; i++) {
                array[index++] = Double.parseDouble(values[i]) * factor;
            }
        }

        return array;
    }


    /**
     * Extract symbols from a DATA TABLE or XYDATA header.
     * <p>
     * Accepted patterns include:
     * <ul>
     * <li>(X++(Y..Y))</li>
     * <li>(X++(R..R)), XYDATA</li>
     * <li>(T2++(R..R)), PROFILE</li>
     * </ul>
     *
     * @param header a data table pattern definition
     * @return two extracted symbols
     * @throws IllegalArgumentException when the symbols differs in the second part, for example "(R..I)"
     */
    public static String[] extractSymbols(String header) {
        Pattern pattern = Pattern.compile("\\((.+)\\+\\+\\((.)\\.\\.(.)\\)\\).*");
        Matcher matcher = pattern.matcher(header);
        if (matcher.matches()) {
            String x = matcher.group(1);
            String y = matcher.group(2);
            String z = matcher.group(3);
            if (!y.equals(z)) {
                throw new IllegalArgumentException("Unable to parse symbols, two different symbols set:" + y + ", " + z);
            }
            return new String[] {x, y};
        }

        throw new IllegalArgumentException("Unsupported data header format: " + header);
    }
}
