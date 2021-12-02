package com.nanalysis.jcamp.model;

import static com.nanalysis.jcamp.util.JCampUtil.normalize;

import java.util.Arrays;

public enum Form {
    UNKNOWN,
    AFFN, // ASCII Free Format Numeric
    ASDF; // ASCII Squeezed Difference Form

    private final String normalized;

    Form() {
        this.normalized = normalize(name());
    }

    public String normalized() {
        return normalized;
    }

    public static Form fromString(String dataType) {
        String normalizedDataType = normalize(dataType);
        return Arrays.stream(values())
            .filter(type -> type.normalized.equals(normalizedDataType))
            .findFirst()
            .orElse(Form.UNKNOWN);
    }
}
