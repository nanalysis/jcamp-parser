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

import com.nanalysis.jcamp.model.JCampRecord;
import com.nanalysis.jcamp.model.Label;
import com.nanalysis.jcamp.model.XYDataPage;

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
