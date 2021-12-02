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
package com.nanalysis.jcamp.model;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class JCampBlockTest {
    @Test
    public void emptyBlockAndDocument() {
        JCampBlock block = new JCampBlock(new JCampDocument());

        assertNotNull(block.getTitle());
        assertNotNull(block.getVersion());
        assertEquals(DataType.UNKNOWN, block.getDataType());
        assertEquals(DataClass.UNKNOWN, block.getDataClass());
        assertEquals(0, block.getPageCount());
    }

    @Test
    public void emptyBlockWithFilledDocument() {
        JCampDocument document = new JCampDocument();
        document.addRecord(new JCampRecord(Label.TITLE.name(), "DOC TITLE"));
        document.addRecord(new JCampRecord(Label.JCAMP_DX.name(), "DOC VERSION"));
        document.addRecord(new JCampRecord(Label.DATA_TYPE.name(), DataType.ND_NMR_FID.name()));
        JCampBlock block = new JCampBlock(document);

        assertEquals("DOC TITLE", block.getTitle());
        assertEquals("DOC VERSION", block.getVersion());
        assertEquals(DataType.ND_NMR_FID, block.getDataType());
        assertEquals(DataClass.UNKNOWN, block.getDataClass());
        assertEquals(0, block.getPageCount());
    }

    @Test
    public void basicAttributesWhenDefinedInBothBlockAndDocument() {
        JCampDocument document = new JCampDocument();
        document.addRecord(new JCampRecord(Label.TITLE.name(), "DOC TITLE"));
        document.addRecord(new JCampRecord(Label.JCAMP_DX.name(), "DOC VERSION"));
        document.addRecord(new JCampRecord(Label.DATA_TYPE.name(), DataType.LINK.name()));

        JCampBlock block = new JCampBlock(document);
        block.addRecord(new JCampRecord(Label.TITLE.name(), "BLOCK TITLE"));
        block.addRecord(new JCampRecord(Label.JCAMP_DX.name(), "BLOCK VERSION"));
        block.addRecord(new JCampRecord(Label.DATA_TYPE.name(), DataType.NMR_FID.name()));
        block.addRecord(new JCampRecord(Label.DATA_CLASS.name(), DataClass.NTUPLES.name()));

        assertEquals("BLOCK TITLE", block.getTitle());
        assertEquals("BLOCK VERSION", block.getVersion());
        assertEquals(DataType.NMR_FID, block.getDataType());
        assertEquals(DataClass.NTUPLES, block.getDataClass());
        assertEquals(0, block.getPageCount());
    }

    @Test
    public void pages() {
        JCampBlock block = new JCampBlock(new JCampDocument());
        JCampPage r1 = addPageToBlock(block, "R");
        JCampPage i1 = addPageToBlock(block, "I");
        JCampPage r2 = addPageToBlock(block, "R");
        JCampPage i2 = addPageToBlock(block, "I");

        assertEquals(4, block.getPageCount());
        assertSame(r1, block.page(0));
        assertSame(i1, block.page(1));
        assertSame(r2, block.page(2));
        assertSame(i2, block.page(3));

        assertEquals(List.of(r1, r2), block.getPagesForYSymbol("R"));
        assertEquals(List.of(i1, i2), block.getPagesForYSymbol("I"));
        assertTrue(block.getPagesForYSymbol("?").isEmpty());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void invalidPageAccess() {
        JCampBlock block = new JCampBlock(new JCampDocument());
        block.page(42);
    }

    private JCampPage addPageToBlock(JCampBlock block, String y) {
        JCampPage page = new JCampPage(block);
        page.addRecord(new JCampRecord(Label.DATA_TABLE.name(), String.format("(X++(%s..%s))", y, y)));
        block.addPage(page);
        return page;
    }
}
