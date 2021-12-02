package com.nanalysis.jcamp.model;

import static com.nanalysis.jcamp.util.JCampUtil.normalize;

import java.util.Arrays;

public enum DataClass {
    UNKNOWN, NTUPLES, XYDATA, PEAK_TABLE;

    private final String normalized;

    DataClass() {
        this.normalized = normalize(name());
    }

    public String normalized() {
        return normalized;
    }

    public static DataClass fromString(String dataType) {
        String normalizedDataType = normalize(dataType);
        return Arrays.stream(values())
            .filter(type -> type.normalized.equals(normalizedDataType))
            .findFirst()
            .orElse(DataClass.UNKNOWN);
    }
}
