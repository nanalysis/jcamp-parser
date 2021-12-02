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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

import org.junit.Test;

public class JCampRecordTest {
    private static final double DELTA = 1e-6;

    @Test(expected = IllegalArgumentException.class)
    public void parseInvalidRecord() {
        JCampRecord.parse("NO EQUALS SIGN");
    }

    @Test
    public void parseSingleValue() {
        String input = "BLOCKS=  2";
        JCampRecord parsed = JCampRecord.parse(input);

        assertEquals("BLOCKS", parsed.getLabel());
        assertEquals("2", parsed.getString());
        assertEquals(2, parsed.getInt());
        assertEquals("", parsed.getComment());
    }

    @Test
    public void parseDataWithComments() {
        String input = "JCAMPDX= 6.0         $$ Spinit NMR compound JCAMP-DX V2.0 - code from SPIKE by CASC4DE";
        JCampRecord parsed = JCampRecord.parse(input);

        assertEquals("JCAMPDX", parsed.getLabel());
        assertEquals("6.0", parsed.getString());
        assertEquals("Spinit NMR compound JCAMP-DX V2.0 - code from SPIKE by CASC4DE", parsed.getComment());
    }

    @Test
    public void parseMultiLine() {
        String input = "$RDC_PARAMETERS=(0,1,";
        List<String> newLines = List.of("2,3,", "4)");
        JCampRecord parsed = JCampRecord.parse(input);
        newLines.forEach(parsed::parseData);

        assertEquals("$RDC_PARAMETERS", parsed.getLabel());
        assertEquals("(0,1,\n2,3,\n4)", parsed.getString());
    }

    @Test
    public void parseMultiLineWithComments() {
        String input = "TEST=a $$ first";
        List<String> newLines = List.of("b $$ second", "c $$ third");
        JCampRecord parsed = JCampRecord.parse(input);
        newLines.forEach(parsed::parseData);

        assertEquals("TEST", parsed.getLabel());
        assertEquals("a\nb\nc", parsed.getString());
        assertEquals("first\nsecond\nthird", parsed.getComment());
    }

    @Test
    public void labelNormalization() {
        JCampRecord parsed = JCampRecord.parse("$.test-ME_now=");
        assertEquals("$.test-ME_now", parsed.getLabel());
        assertEquals("$.TESTMENOW", parsed.getNormalizedLabel());
    }

    @Test
    public void intConversion() {
        JCampRecord single = new JCampRecord("TEST", "42");
        JCampRecord floatingPoint = new JCampRecord("TEST", "4.2");
        JCampRecord multiple = new JCampRecord("TEST", "(5,6,\n 7,8)");
        assertEquals(42, single.getInt());
        assertEquals(4, floatingPoint.getInt());
        assertEquals(5, multiple.getInt());
        assertArrayEquals(new int[] {42}, single.getInts());
        assertArrayEquals(new int[] {5, 6, 7, 8}, multiple.getInts());
    }

    @Test
    public void doubleConversion() {
        JCampRecord single = new JCampRecord("TEST", "4.2");
        JCampRecord multiple = new JCampRecord("TEST", "(5.6,\n 7.8, 9.01e2)");
        assertEquals(4.2, single.getDouble(), DELTA);
        assertArrayEquals(new double[] {4.2}, single.getDoubles(), DELTA);
        assertArrayEquals(new double[] {5.6, 7.8, 901}, multiple.getDoubles(), DELTA);
    }

    @Test
    public void dateConversion() {
        JCampRecord benchtop = new JCampRecord("TEST", "2021/09/09 15:54:27-0700");
        JCampRecord cascade = new JCampRecord("TEST", "2021-05-17T17:22:46.144+02:00");
        JCampRecord bruker = new JCampRecord("TEST", "2019/11/04 22:12:02+0000");
        JCampRecord timestamp = new JCampRecord("TEST", "1572905522");

        assertEquals("benchtop date format", new Date(1631228067000L), benchtop.getDate());
        assertEquals("cascade export date format", new Date(1621264966144L), cascade.getDate());
        assertEquals("bruker date format", new Date(1572905522000L), bruker.getDate());
        assertEquals("timestamp", new Date(1572905522000L), timestamp.getDate());
    }

    @Test(expected = IllegalStateException.class)
    public void invalidDateConversion() {
        JCampRecord invalid = new JCampRecord("TEST", "not a date");
        invalid.getDate();
    }
}
