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
package com.nanalysis.jcamp.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.nanalysis.jcamp.model.DataType;
import com.nanalysis.jcamp.model.JCampDocument;
import org.junit.Test;

import com.nanalysis.jcamp.model.DataClass;

import java.io.IOException;

public class JCampParserTest {
    @Test
    public void linkDataTypeWithSeveralBlocks() {
        JCampDocument data = new JCampParser().parse(resourceAsString("/spinit/cascade/demo_HSQC_ET_GS_GARP-4_4_0.dx"));
        assertEquals("#4 - HSQC_ET_GS_GARP-4 -  rs2d", data.getTitle());
        assertEquals("6.0", data.getVersion());
        assertEquals(DataType.LINK, data.getDataType());
        assertEquals(2, data.getBlockCount());
        assertTrue(data.containsDeclaredNumberOfBlocks());
        assertEquals(DataType.ND_NMR_FID, data.block(0).getDataType());
        assertEquals(DataType.ND_NMR_SPECTRUM, data.block(1).getDataType());
    }

    @Test
    public void fidDataType1D() {
        JCampDocument data = new JCampParser().parse(resourceAsString("/benchtop/100/NMReady_1D_1H_20210302_quinine_4.dx"));
        assertEquals("NMReady_1D_1H_20210302_quinine_4", data.getTitle());
        assertEquals("5.01", data.getVersion());
        assertEquals(DataType.NMR_FID, data.getDataType());
        assertEquals(1, data.getBlockCount());
        assertTrue(data.containsDeclaredNumberOfBlocks());
        assertEquals(DataType.NMR_FID, data.block(0).getDataType());
        assertEquals(DataClass.NTUPLES, data.block(0).getDataClass());
    }

    @Test
    public void fidDataType2D() {
        JCampDocument data = new JCampParser().parse(resourceAsString("/benchtop/100/NMReady_COSY_1H_20210324_dep_64x512.dx"));
        assertEquals("NMReady_COSY_1H_20210324_dep_64x512", data.getTitle());
        assertEquals("6.0", data.getVersion());
        assertEquals(DataType.ND_NMR_FID, data.getDataType());
        assertEquals(1, data.getBlockCount());
        assertTrue(data.containsDeclaredNumberOfBlocks());
        assertEquals(DataType.ND_NMR_FID, data.block(0).getDataType());
        assertEquals(DataClass.NTUPLES, data.block(0).getDataClass());
    }

    @Test
    public void spectrumDataType1D() {
        JCampDocument data = new JCampParser().parse(resourceAsString(
            "/benchtop/60/NMReady_1D_1H_20210909_Test_formatesS.jdx"));
        assertEquals("NMReady_1D_1H_20210909_Test_formates", data.getTitle());
        assertEquals("5.01", data.getVersion());
        assertEquals(DataType.NMR_SPECTRUM, data.getDataType());
        assertEquals(1, data.getBlockCount());
        assertTrue(data.containsDeclaredNumberOfBlocks());
        assertEquals(DataType.NMR_SPECTRUM, data.block(0).getDataType());
        assertEquals(DataClass.XYDATA, data.block(0).getDataClass());
    }

    @Test
    public void checkBlockInformation() {
        JCampDocument data = new JCampParser().parse(resourceAsString("/spinit/cascade/demo_HSQC_ET_GS_GARP-4_4_0.dx"));
        assertEquals(2, data.getBlockCount());
        assertTrue(data.containsDeclaredNumberOfBlocks());
        assertEquals("#4 - HSQC_ET_GS_GARP-4 -  rs2d", data.block(0).getTitle());
        assertEquals("6.0", data.block(0).getVersion());
        assertEquals(DataType.ND_NMR_FID, data.block(0).getDataType());
        assertEquals(DataClass.NTUPLES, data.block(0).getDataClass());
        assertEquals("#4 - HSQC_ET_GS_GARP-4 -  rs2d", data.block(1).getTitle());
        assertEquals("6.0", data.block(1).getVersion());
        assertEquals(DataType.ND_NMR_SPECTRUM, data.block(1).getDataType());
        assertEquals(DataClass.NTUPLES, data.block(1).getDataClass());
    }

    @Test
    public void checkRecordDimension() {
        JCampDocument data = new JCampParser().parse(resourceAsString("/benchtop/100/NMReady_COSY_1H_20210324_dep_64x512.dx"));
        assertEquals(1, data.getBlockCount());
        assertTrue(data.containsDeclaredNumberOfBlocks());
        assertEquals(512, data.block(0).get("$SI", 0).getInt());
        assertEquals(64, data.block(0).get("$SI", 1).getInt());
    }

    @Test
    public void checkMultiplePages() {
        JCampDocument data = new JCampParser().parse(resourceAsString("/benchtop/100/NMReady_COSY_1H_20210324_dep_64x512.dx"));
        assertEquals(1, data.getBlockCount());
        assertTrue(data.containsDeclaredNumberOfBlocks());
        assertEquals(128, data.block(0).getPageCount());
    }

    @Test(expected = IllegalStateException.class)
    public void checkValueOutOfDocument() {
        new JCampParser().parse("##END=\n##FAILURE=document ended");
    }

    private String resourceAsString(String name) {
        try (var input = getClass().getResourceAsStream(name)) {
            if(input == null) {
                throw new IllegalStateException("No resource found for " + name);
            }

            return new String(input.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
