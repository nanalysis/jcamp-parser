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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.nanalysis.jcamp.model.JCampDocument;
import com.nanalysis.jcamp.model.JCampRecord;
import com.nanalysis.jcamp.parser.builder.DocumentBuilder;
import com.nanalysis.jcamp.parser.builder.JCampBuilder;

public class JCampParser {
    public static final String ENTRY_PREFIX = "##";
    public static final String COMMENT_PREFIX = "$$";

    private int lineNumber;
    private JCampBuilder<?> currentBuilder;
    private JCampRecord currentEntry;

    public JCampDocument parse(File file) throws IOException {
        return parse(Files.readString(file.toPath()));
    }

    public JCampDocument parse(String input) {
        DocumentBuilder documentBuilder = new DocumentBuilder();
        this.currentBuilder = documentBuilder;

        lineNumber = 0;
        input.lines()
            .map(String::trim)
            .forEach(this::parseLine);
        return documentBuilder.getObject();
    }

    private void parseLine(String line) {
        lineNumber++;

        if (line.isEmpty()) {
            // skip empty lines
            return;
        }

        if (line.startsWith(ENTRY_PREFIX)) {
            currentEntry = JCampRecord.parse(lineNumber, line.substring(2));
            currentBuilder = currentBuilder.consume(currentEntry);
        } else if (line.startsWith(COMMENT_PREFIX)) {
            String comment = line.substring(2);
            currentBuilder = currentBuilder.consumeComment(comment);
        } else if (currentEntry != null) {
            currentEntry.parseData(line);
        }
    }
}
