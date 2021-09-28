package rs2d.jcamp.model;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a single XYDATA record as a page.
 * This may be an oversimplification, but is working to open benchtop data.
 */
public class XYDataPage extends JCampPage {
    public XYDataPage(JCampContainer parent) {
        super(parent);
    }

    @Override
    public String getHeader() {
        return get(Label.XYDATA).getString().lines().findFirst()
            .orElseThrow(() -> new IllegalStateException("Empty data header!"));
    }

    @Override
    public List<String> getDataLines() {
        return get(Label.XYDATA).getString().lines().skip(1)
            .collect(Collectors.toList());
    }

    @Override
    public String extractPageSymbol() {
        return "N"; // fake a single page, with N=1
    }

    @Override
    public String extractPageValue() {
        return "1"; // fake a single page, with N=1
    }

    @Override
    protected Form getFormForSymbol(String symbol) {
        // TODO ADSF support for XYDATA blocks - does this exist? then which parameter defines this form?
        return Form.ASDF; // default in case it is not specified
    }

    @Override
    protected int getDimensionForSymbol(String symbol) {
        return parent.getOrDefault(Label.NPOINTS, "0").getInt();
    }

    @Override
    protected double getFactorForSymbol(String symbol) {
        return parent.getOrDefault(symbol + "FACTOR", "1").getDouble();
    }
}
