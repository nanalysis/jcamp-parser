package rs2d.jcamp.parser.builder;

import rs2d.jcamp.model.JCampDocument;
import rs2d.jcamp.model.JCampRecord;
import rs2d.jcamp.model.Label;

/**
 * Builds a complete JCamp document.
 */
public class DocumentBuilder implements JCampBuilder<JCampDocument> {
    private static final String END_OF_DOCUMENT = "END";

    private final JCampDocument root = new JCampDocument();

    @Override
    public JCampDocument getObject() {
        return root;
    }

    @Override
    public JCampBuilder<?> consume(JCampRecord record) {
        if (END_OF_DOCUMENT.equals(record.getNormalizedLabel())) {
            return new ErrorBuilder("After document end!");
        }

        if (root.getDataType().isSingleBlock() || root.contains(Label.BLOCKS)) {
            BlockBuilder builder = new BlockBuilder(this);
            root.addBlock(builder.getObject());
            return builder.consume(record);
        }

        // default: add to root document
        root.addRecord(record);
        return this;
    }

}
