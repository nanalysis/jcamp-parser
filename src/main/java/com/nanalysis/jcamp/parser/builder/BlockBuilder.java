package com.nanalysis.jcamp.parser.builder;

import com.nanalysis.jcamp.model.JCampDocument;
import com.nanalysis.jcamp.model.JCampBlock;
import com.nanalysis.jcamp.model.JCampRecord;
import com.nanalysis.jcamp.model.Label;

/**
 * Builds a JCamp block.
 */
public class BlockBuilder implements JCampBuilder<JCampBlock> {
    private static final String END_OF_BLOCK = "END";

    private final DocumentBuilder parent;
    private final JCampBlock block;

    public BlockBuilder(DocumentBuilder parent) {
        this.parent = parent;
        this.block = new JCampBlock(parent.getObject());
    }

    @Override
    public JCampBlock getObject() {
        return this.block;
    }

    @Override
    public JCampBuilder<?> consume(JCampRecord record) {
        if (Label.PAGE.normalized().equals(record.getNormalizedLabel())) {
            // start building the first page
            PageBuilder firstPage = new PageBuilder(this);
            block.addPage(firstPage.getObject());
            return firstPage.consume(record);
        }

        if (Label.XYDATA.normalized().equals(record.getNormalizedLabel())) {
            XYDataPageBuilder xydata = new XYDataPageBuilder(this);
            block.addPage(xydata.getObject());
            return xydata.consume(record);
        }

        if (END_OF_BLOCK.equals(record.getNormalizedLabel())) {
            JCampDocument document = parent.getObject();
            if (!document.containsDeclaredNumberOfBlocks()) {
                // previous block ended, add a new one
                BlockBuilder builder = new BlockBuilder(parent);
                document.addBlock(builder.getObject());
                return builder;
            } else {
                // all blocks are added, return back to parent context
                return parent;
            }
        }

        // default: add to current block and keep consuming entries
        block.addRecord(record);
        return this;
    }
}
