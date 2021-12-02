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

public class JCampUtil {
    private JCampUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * JCamp-DX entry labels (and even values) are not always written the same way.
     * This removed all whitespace, underscores, dashes, and converts to uppercase, for easy comparisons.
     *
     * @param s a string
     * @return an uppercase string without whitespace, underscore, or dash.
     */
    public static String normalize(String s) {
        if (s == null) {
            return "";
        }

        return s.toUpperCase()
            .replace(" ", "")
            .replace("\t", "")
            .replace("_", "")
            .replace("-", "")
            .replace("/", "");
    }

    /**
     * Sometimes, nuclei names are written as "^1H" or "<1H>", when spinlab expects only "1H".
     *
     * @param nucleus a nucleus name from jcamp
     * @return a nucleus name without unwanted characters.
     */
    public static String toNucleusName(String nucleus) {
        if (nucleus == null) {
            return "";
        }

        return nucleus.replace("^", "").replace("<", "").replace(">", "");
    }

    /**
     * Removes parenthesis from a string.
     *
     * @param data the input data
     * @return the data without parenthesis.
     */
    public static String withoutParenthesis(String data) {
        if (data == null) {
            return "";
        }

        return data.replace("(", "").replace(")", "");
    }

    /**
     * Removes brackets from a string.
     *
     * @param data the input data
     * @return the data without brackets.
     */
    public static String withoutBrackets(String data) {
        if (data == null) {
            return "";
        }

        return data.replace("[", "").replace("]", "");
    }
}
