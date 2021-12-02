/*
 * JCamp-Parser: a basic parsing library
 * Copyright (C) 2021 - Nanalysis Scientific Corp.
 * -
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
