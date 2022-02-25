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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ASDF (ASCII Squeeze Difference Form) parser.
 */
public class ASDFParser {
    private final static String POSITIVE_SQZ = "@ABCDEFGHI";
    private final static String NEGATIVE_SQZ = "@abcdefghi";
    private final static String POSITIVE_DIF = "%JKLMNOPQR";
    private final static String NEGATIVE_DIF = "%jklmnopqr";
    private final static String DUP = "?STUVWXYZs";

    // tokenize on first numerical value, then each time a special ASDF char is found: alpha, @ and %.
    private final static Pattern TOKENIZER = Pattern.compile("([\\d.]+)|([A-Za-z@%]\\d*)");

    enum TokenType {
        SQZ, DIF, DUP, NUMERICAL
    }

    enum Mode {
        SQZ, DIF
    }

    private final int[] data;
    private boolean xMatchesIndex;
    private int index;
    private Mode mode;

    public ASDFParser(int size) {
        this.data = new int[size];
        this.xMatchesIndex = true;
    }

    protected int[] getData() {
        return this.data;
    }

    /**
     * Parse a complete ASDF block, already split in lines.
     * Note that this implementation ignores the X values, and only stores the Y values. In implementation terms, this means that the first token of
     * each line is ignored.
     *
     * @param lines ASDF lines to parse
     * @return the corresponding integer values.
     */
    public int[] parse(List<String> lines) {
        if (index != 0) {
            throw new IllegalStateException("Trying to reuse a parser that has already consumed data!");
        }

        for (String line : lines) {
            try {
                parseSingleLine(line);
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Unable to parse line: " + line, e);
            }
        }

        if (index != data.length) {
            // This happens on some data, from cascade's spike-based export, mostly on FIDs.
            // Best guess is that this export don't write trailing zeros...
            System.out.println("Missing data, was expecting " + data.length + " points, read only " + index);
        }
        return data;
    }

    /**
     * Parse a single ASDF line, append its content to the current data.
     *
     * @param line a line of text in ASDF format
     */
    protected void parseSingleLine(String line) {
        List<String> tokens = tokenize(line);
        if (tokenType(tokens.get(0)) != TokenType.NUMERICAL) {
            throw new IllegalArgumentException("Expected to start with a numerical token, received: " + tokens.get(0));
        }

        int x = intValue(tokens.get(0));
        int startAt = 1; // ignore the first token, it corresponds to X value

        // some implementations don't repeat the previous value, even if the spec says they should.
        // let's try to detect them when their x value indicates the next index
        // x can be previous index when last token of previous line was of DIF type, to allow for repeating the last value (Y-check)
        xMatchesIndex = xMatchesIndex && ((x == index) || (mode == Mode.DIF && x == index - 1));
        boolean shouldSkipYCheck = xMatchesIndex && (x == index);

        // when a line ends on DIF mode, the next line is supposed to repeat the same value. (Y value check)
        if (mode == Mode.DIF && tokens.size() > 1 && index > 1 && !shouldSkipYCheck) {
            String token = tokens.get(1);
            if (tokenType(token) != TokenType.SQZ) {
                throw new IllegalArgumentException("Expected a SQZ token to start a line after a DIF, received: " + token);
            }

            int value = intValue(token);
            if (value != data[index - 1]) {
                throw new IllegalStateException(
                    "Check failed, value after last DIF isn't what expected. Received: " + value + " but expected " + data[index - 1]);
            }

            startAt = 2; // check done, ignore this token, value already consumed.
        }

        for (int i = startAt; i < tokens.size(); i++) {
            String token = tokens.get(i);
            TokenType type = tokenType(token);
            int value = intValue(token);
            if (type == TokenType.SQZ) { // normal "squeezed" value
                data[index] = value;
                mode = Mode.SQZ;
                index++;
            } else if (type == TokenType.DIF && index > 0) { // differential value
                data[index] = data[index - 1] + value;
                mode = Mode.DIF;
                index++;
            } else if (type == TokenType.DUP && index > 0) { // duplicate value
                int copies = value - 1; // duplicate count include already written value
                if (mode == Mode.SQZ) { // duplicate previous value
                    for (int r = 0; r < copies; r++) {
                        data[index] = data[index - 1];
                        index++;
                    }
                } else if (mode == Mode.DIF && index > 1) { // duplicate difference between values
                    int diff = data[index - 1] - data[index - 2];
                    for (int r = 0; r < copies; r++) {
                        data[index] = data[index - 1] + diff;
                        index++;
                    }
                } else {
                    throw new IllegalArgumentException("Unexpected DUP token, current mode is " + mode + ": " + token);
                }
            } else if (type == TokenType.NUMERICAL) {
                throw new IllegalArgumentException("Unexpected numerical token: " + token);
            } else if (index == 0) {
                throw new IllegalArgumentException("Unexpected " + type + " token for first value: " + token);
            }
        }
    }

    /**
     * Split a ASDF line in individual tokens. This doesn't try to interpret or convert them, it does only split the input string.
     *
     * @param line a line of text in ASDF format
     * @return a list of tokens.
     */
    protected List<String> tokenize(String line) {
        List<String> tokens = new ArrayList<>();

        Matcher matcher = TOKENIZER.matcher(line);
        while (matcher.find()) {
            tokens.add(matcher.group(0));
        }

        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Unable to tokenize line: " + line);
        }

        return tokens;
    }

    /**
     * Detect the token type, based on its first character.
     *
     * @param token an ASDF token
     * @return the corresponding token type.
     */
    protected TokenType tokenType(String token) {
        if (token.isEmpty()) {
            throw new IllegalArgumentException("Empty token!");
        }

        char first = token.charAt(0);
        if (POSITIVE_SQZ.indexOf(first) >= 0 || NEGATIVE_SQZ.indexOf(first) >= 0)
            return TokenType.SQZ;
        if (POSITIVE_DIF.indexOf(first) >= 0 || NEGATIVE_DIF.indexOf(first) >= 0)
            return TokenType.DIF;
        if (DUP.indexOf(first) >= 0)
            return TokenType.DUP;
        return TokenType.NUMERICAL;
    }

    /**
     * Converts a token to an integer.
     * 
     * @param token an ASDF token
     * @return the corresponding integer.
     */
    protected int intValue(String token) {
        try {
            return Integer.parseInt(asdfToAscii(token));
        } catch (NumberFormatException e) {
            // maybe the integer was formatted as a floating point number?
            return (int) Math.round(doubleValue(token));
        }
    }


    /**
     * Converts a token to a double. This makes sense for the first token, which is not supposed to be in ASDF form.
     * 
     * @param token an ASDF token or a direct numerical token
     * @return the corresponding double.
     */
    protected double doubleValue(String token) {
        return Double.parseDouble(asdfToAscii(token));
    }

    /**
     * ASDF tokens are made of a special char indicating both the token type, the first digit value and its sign, followed by other digits in ASCII.
     * This converts the whole token to an ASCII string.
     *
     * @param token an ASDF token
     * @return the corresponding first digit in ASCII with an optional sign.
     */
    private String asdfToAscii(String token) {
        if (token.isEmpty()) {
            throw new IllegalArgumentException("Empty token!");
        }
        if (token.length() == 1)
            return convertFirstChar(token.charAt(0));

        return convertFirstChar(token.charAt(0)) + token.substring(1);
    }

    /**
     * ASDF tokens are made of a special char indicating both the token type, the first digit value and its sign, followed by other digits in ASCII.
     * This converts only the first special character into an ASCII digit with an optional minus sign for negative numbers.
     * 
     * @param firstChar the first character of a ASDF token
     * @return the corresponding first digit in ASCII with an optional sign.
     */
    private String convertFirstChar(char firstChar) {
        String[] positives = {POSITIVE_SQZ, POSITIVE_DIF, DUP};
        for (String positive : positives) {
            int index = positive.indexOf(firstChar);
            if (index >= 0) {
                return String.valueOf(index);
            }
        }

        String[] negatives = {NEGATIVE_SQZ, NEGATIVE_DIF};
        for (String negative : negatives) {
            int index = negative.indexOf(firstChar);
            if (index >= 0) {
                return String.valueOf(-index);
            }
        }

        return String.valueOf(firstChar);
    }
}
