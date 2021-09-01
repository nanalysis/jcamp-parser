package rs2d.jcamp.model;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import rs2d.jcamp.parser.ASDFParser;

// TODO javadoc + text, explain page structure for XYDATA fakery
public class JCampPage extends JCampContainer {
    protected final JCampContainer parent;

    public JCampPage(JCampContainer parent) {
        this.parent = parent;
    }

    public String getHeader() {
        return get(Label.DATA_TABLE).getString().lines().findFirst()
            .orElseThrow(() -> new IllegalStateException("Empty data header!"));
    }

    public List<String> getDataLines() {
        return get(Label.DATA_TABLE).getString().lines().skip(1)
            .collect(Collectors.toList());
    }

    public String extractPageSymbol() {
        String pageValue = get(Label.PAGE).getString();
        return pageValue.split("=", 2)[0];
    }

    public String extractPageValue() {
        String pageValue = get(Label.PAGE).getString();
        return pageValue.split("=", 2)[1];
    }

    public double extractPageValueAsNumber() {
        return Double.parseDouble(extractPageValue());
    }

    public String extractXSymbol() {
        return extractSymbols(getHeader())[0];
    }

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

    protected String getFormForSymbol(String symbol) {
        return getAttributeForSymbol(Label.VAR_FORM, symbol, "AFFN");
    }

    protected int getDimensionForSymbol(String symbol) {
        String dim = getAttributeForSymbol(Label.VAR_DIM, symbol, "1");
        return Integer.parseInt(dim);
    }

    protected double getFactorForSymbol(String symbol) {
        String factor = getAttributeForSymbol(Label.FACTOR, symbol, "1");
        return Double.parseDouble(factor);
    }

    protected double getFirstForSymbol(String symbol) {
        String first = getAttributeForSymbol(Label.FIRST, symbol, "0");
        return Double.parseDouble(first);
    }

    protected double getLastForSymbol(String symbol) {
        String last = getAttributeForSymbol(Label.LAST, symbol, "0");
        return Double.parseDouble(last);
    }

    public double[] toArray() {
        String[] symbols = extractSymbols(getHeader());
        String xSymbol = symbols[0];
        String ySymbol = symbols[1];

        int size = getDimensionForSymbol(xSymbol);

        String form = getFormForSymbol(ySymbol);
        // TODO form enum?
        if (form.equals("AFFN")) {
            return affnToArray(ySymbol, size);
        } else if (form.equals("ASDF")) {
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
