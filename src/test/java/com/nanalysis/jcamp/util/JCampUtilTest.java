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
package com.nanalysis.jcamp.util;

import static org.junit.Assert.assertEquals;
import static com.nanalysis.jcamp.util.JCampUtil.*;

import org.junit.Test;

public class JCampUtilTest {
    @Test
    public void normalization() {
        assertEquals("", normalize(null));
        assertEquals("", normalize(""));
        assertEquals("ABCD", normalize("ABCD"));
        assertEquals("DEFG", normalize("defg"));
        assertEquals("HIJK", normalize("hi-jk"));
        assertEquals("LMNOPQ", normalize("lm nop_Q"));
    }

    @Test
    public void nucleusName() {
        assertEquals("", toNucleusName(null));
        assertEquals("", toNucleusName(""));
        assertEquals("1H", toNucleusName("1H"));
        assertEquals("13C", toNucleusName("<13C>"));
        assertEquals("Si29", toNucleusName("^Si29"));
    }

    @Test
    public void parenthesisRemoval() {
        assertEquals("", withoutParenthesis(null));
        assertEquals("", withoutParenthesis(""));
        assertEquals("test", withoutParenthesis("test"));
        assertEquals("outside", withoutParenthesis("(outside)"));
        assertEquals("inSIDe", withoutParenthesis("in(SID)e"));
        assertEquals("mul ti ple", withoutParenthesis("(mul (ti) pl)e"));
        assertEquals("unmatched", withoutParenthesis("un(((match)ed"));
    }

    @Test
    public void bracketsRemoval() {
        assertEquals("", withoutBrackets(null));
        assertEquals("", withoutBrackets(""));
        assertEquals("test", withoutBrackets("test"));
        assertEquals("outside", withoutBrackets("[outside]"));
        assertEquals("inSIDe", withoutBrackets("in[SID]e"));
        assertEquals("mul ti ple", withoutBrackets("[mul [ti] pl]e"));
        assertEquals("unmatched", withoutBrackets("un[[[match]ed"));
    }
}
