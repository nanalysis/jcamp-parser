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

import java.util.NoSuchElementException;

import org.junit.Test;

public class JCampDocumentTest {
    @Test
    public void emptyDocument() {
        JCampDocument document = new JCampDocument();
        assertNotNull(document.getTitle());
        assertNotNull(document.getVersion());
        assertEquals(DataType.UNKNOWN, document.getDataType());
        assertEquals("Documents should contain a single block unless specified otherwise", 1, document.getBlockCount());
        assertFalse("Documents contain no block until some are added during construction", document.containsDeclaredNumberOfBlocks());
    }

    @Test
    public void basicAttributes() {
        JCampDocument document = new JCampDocument();
        document.addRecord(new JCampRecord(Label.TITLE.name(), "TEST TITLE"));
        document.addRecord(new JCampRecord(Label.JCAMP_DX.name(), "TEST VERSION"));
        document.addRecord(new JCampRecord(Label.DATA_TYPE.name(), DataType.ND_NMR_FID.name()));
        document.addRecord(new JCampRecord(Label.BLOCKS.name(), "42"));

        assertEquals("TEST TITLE", document.getTitle());
        assertEquals("TEST VERSION", document.getVersion());
        assertEquals(DataType.ND_NMR_FID, document.getDataType());
        assertEquals(42, document.getBlockCount());
        assertFalse("No block added yet", document.containsDeclaredNumberOfBlocks());
    }

    @Test
    public void blocks() {
        JCampDocument document = new JCampDocument();
        document.addRecord(new JCampRecord(Label.BLOCKS.name(), "2"));
        JCampBlock fid = addBlockOfType(document, DataType.NMR_FID);
        JCampBlock spectrum = addBlockOfType(document, DataType.NMR_SPECTRUM);

        assertEquals(2, document.getBlockCount());
        assertTrue(document.containsDeclaredNumberOfBlocks());
        assertSame(fid, document.block(0));
        assertSame(fid, document.getFirstFid());
        assertSame(spectrum, document.block(1));
        assertSame(spectrum, document.getFirstSpectrum());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void invalidBlockAccess() {
        JCampDocument document = new JCampDocument();
        document.block(42);
    }

    @Test(expected = NoSuchElementException.class)
    public void noFidBlock() {
        JCampDocument document = new JCampDocument();
        addBlockOfType(document, DataType.NMR_SPECTRUM);
        document.getFirstFid();
    }

    @Test(expected = NoSuchElementException.class)
    public void noSpectrumBlock() {
        JCampDocument document = new JCampDocument();
        addBlockOfType(document, DataType.NMR_FID);
        document.getFirstSpectrum();
    }

    @Test
    public void severalFidBlocks() {
        JCampDocument document = new JCampDocument();
        JCampBlock first = addBlockOfType(document, DataType.ND_NMR_FID);
        JCampBlock second = addBlockOfType(document, DataType.NMR_FID);
        assertSame(first, document.getFirstFid());
        assertSame(second, document.block(1));
    }

    @Test
    public void severalSpectrumBlock() {
        JCampDocument document = new JCampDocument();
        JCampBlock first = addBlockOfType(document, DataType.NMR_SPECTRUM);
        JCampBlock second = addBlockOfType(document, DataType.ND_NMR_SPECTRUM);
        assertSame(first, document.getFirstSpectrum());
        assertSame(second, document.block(1));
    }

    private JCampBlock addBlockOfType(JCampDocument document, DataType type) {
        JCampBlock block = new JCampBlock(document);
        block.addRecord(new JCampRecord(Label.DATA_TYPE.name(), type.name()));
        document.addBlock(block);
        return block;
    }
}
