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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class JCampPageTest {
    private static final double DELTA = 1e-6;

    @Test
    public void extractSymbolsFromKnownPageDefinitions() {
        assertEquals("Should be able to parse without type information",
            List.of("X", "Y"), Arrays.asList(JCampPage.extractSymbols("(X++(Y..Y))")));
        assertEquals("Should be able to parse XYDATA",
            List.of("X", "R"), Arrays.asList(JCampPage.extractSymbols("(X++(R..R)), XYDATA")));
        assertEquals("Should be able to parse PROFILE",
            List.of("T2", "I"), Arrays.asList(JCampPage.extractSymbols("(T2++(I..I)), PROFILE")));
    }

    @Test
    public void extractVariableFor1dFid() {
        JCampPage page = new JCampPage(null);
        page.addRecord(new JCampRecord("DATA TABLE", "(X++(I..I)), XYDATA"));
        assertEquals("X", page.extractXSymbol());
        assertEquals("I", page.extractYSymbol());
    }

    @Test
    public void extractVariableFor2dFid() {
        JCampPage page = new JCampPage(null);
        page.addRecord(new JCampRecord("DATA TABLE", "(T2++(R..R)), PROFILE"));
        assertEquals("T2", page.extractXSymbol());
        assertEquals("R", page.extractYSymbol());
    }

    @Test
    public void extractValues() {
        JCampBlock parent = new JCampBlock(null);
        parent.addRecord(new JCampRecord(Label.SYMBOL.name(), "T, R, I"));
        parent.addRecord(new JCampRecord(Label.VAR_DIM.name(), "12, 12, 12"));

        JCampPage page = new JCampPage(parent);
        page.addRecord(new JCampRecord("DATA TABLE", "(T++(R..R)), XYDATA\n"
            + "0.00000000     -517905556    -2147482999     -901597641      179005696\n"
            + "16.00000000      725417901      218517690     -393853179     -665104755\n"
            + "32.00000000     -310803688      117493285      281062089       84565528\n"));

        double[] array = page.toArray();
        assertEquals(12, array.length);
        assertEquals(-517905556, (int) array[0]);
        assertEquals(-2147482999, (int) array[1]);
        assertEquals(725417901, (int) array[4]);
        assertEquals(84565528, (int) array[11]);
    }

    @Test
    public void extractValuesWithFactor() {
        JCampBlock parent = new JCampBlock(null);
        parent.addRecord(new JCampRecord(Label.SYMBOL.name(), "X,R,I,N"));
        parent.addRecord(new JCampRecord(Label.VAR_DIM.name(), "12, 12, 12, 2"));
        parent.addRecord(new JCampRecord(Label.FACTOR.name(), "0.00049940480000000001,0.00002428885732078813,0.00002428885732078813,1"));

        JCampPage page = new JCampPage(parent);
        page.addRecord(new JCampRecord("DATA TABLE", "(X++(R..R)), XYDATA\n"
            + "0.00000000     -517905556    -2147482999     -901597641      179005696\n"
            + "16.00000000      725417901      218517690     -393853179     -665104755\n"
            + "32.00000000     -310803688      117493285      281062089       84565528\n"));

        double[] array = page.toArray();
        assertEquals(12, array.length);
        assertEquals(-12579.334155327448, array[0], DELTA);
        assertEquals(-52159.9081615292, array[1], DELTA);
        assertEquals(17619.57189533461, array[4], DELTA);
        assertEquals(2054.000043849114, array[11], DELTA);
    }
}
