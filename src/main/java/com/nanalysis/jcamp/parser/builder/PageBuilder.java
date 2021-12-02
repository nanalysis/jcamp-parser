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

import com.nanalysis.jcamp.model.JCampPage;
import com.nanalysis.jcamp.model.JCampRecord;
import com.nanalysis.jcamp.model.Label;

/**
 * Builds a JCamp page.
 */
public class PageBuilder implements JCampBuilder<JCampPage> {
    private static final String END_OF_NTUPLES = "ENDNTUPLES";

    private final BlockBuilder parent;
    private final JCampPage page;

    public PageBuilder(BlockBuilder parent) {
        this.parent = parent;
        this.page = new JCampPage(parent.getObject());
    }

    @Override
    public JCampPage getObject() {
        return this.page;
    }

    @Override
    public JCampBuilder<?> consume(JCampRecord record) {
        if (page.contains(record.getLabel()) && Label.PAGE.normalized().equals(record.getNormalizedLabel())) {
            // PAGE already defined, received a new PAGE record => create a new one and build it.
            PageBuilder nextPage = new PageBuilder(parent);
            parent.getObject().addPage(nextPage.getObject());
            return nextPage.consume(record);
        } else if (END_OF_NTUPLES.equals(record.getNormalizedLabel())) {
            // end of page, continue with parent context
            return parent;
        }

        // default: add to current page
        page.addRecord(record);
        return this;
    }
}
