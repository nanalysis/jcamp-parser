package rs2d.jcamp.parser.builder;

import rs2d.jcamp.model.JCampRecord;
import rs2d.jcamp.model.Label;
import rs2d.jcamp.model.XYDataPage;

/**
 * Builds a JCamp page.
 */
public class XYDataPageBuilder implements JCampBuilder<XYDataPage> {
    private final BlockBuilder parent;
    private final XYDataPage page;

    public XYDataPageBuilder(BlockBuilder parent) {
        this.parent = parent;
        this.page = new XYDataPage(parent.getObject());
    }

    @Override
    public XYDataPage getObject() {
        return this.page;
    }

    @Override
    public JCampBuilder<?> consume(JCampRecord record) {
        if (!Label.XYDATA.normalized().equals(record.getNormalizedLabel())) {
            return new ErrorBuilder("Only XYDATA is accepted here").consume(record);
        }

        page.addRecord(record);
        return parent;
    }
}
