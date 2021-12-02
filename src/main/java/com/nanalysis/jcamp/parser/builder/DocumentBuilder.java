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
import com.nanalysis.jcamp.model.JCampRecord;
import com.nanalysis.jcamp.model.Label;

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
