package rs2d.jcamp.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import rs2d.jcamp.model.JCampDocument;
import rs2d.jcamp.model.JCampRecord;
import rs2d.jcamp.parser.builder.DocumentBuilder;
import rs2d.jcamp.parser.builder.JCampBuilder;

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
