package rs2d.jcamp.parser.builder;

import rs2d.jcamp.model.JCampRecord;

public class ErrorBuilder implements JCampBuilder<String> {
    private final String message;

    public ErrorBuilder(String message) {
        this.message = message;
    }

    @Override
    public String getObject() {
        return message;
    }

    @Override
    public JCampBuilder<?> consume(JCampRecord record) {
        throw new IllegalStateException("Line " + record.getLineNumber() + ", unexpected record: "
            + record.getLabel() + ", " + message);
    }
}
