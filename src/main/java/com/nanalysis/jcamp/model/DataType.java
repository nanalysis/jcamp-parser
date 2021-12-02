package com.nanalysis.jcamp.model;

import static com.nanalysis.jcamp.util.JCampUtil.normalize;

import java.util.Arrays;

public enum DataType {
    UNKNOWN, LINK, NMR_FID, ND_NMR_FID, NMR_SPECTRUM, ND_NMR_SPECTRUM;

    private final String normalized;

    DataType() {
        this.normalized = normalize(name());
    }

    public String normalized() {
        return normalized;
    }

    public boolean isFID() {
        return this == NMR_FID || this == ND_NMR_FID;
    }

    public boolean isSpectrum() {
        return this == NMR_SPECTRUM || this == ND_NMR_SPECTRUM;
    }

    public boolean isSingleBlock() {
        return this != UNKNOWN && this != LINK;
    }

    public static DataType fromString(String dataType) {
        String normalizedDataType = normalize(dataType);
        return Arrays.stream(values())
            .filter(type -> type.normalized.equals(normalizedDataType))
            .findFirst()
            .orElse(DataType.UNKNOWN);
    }
}
