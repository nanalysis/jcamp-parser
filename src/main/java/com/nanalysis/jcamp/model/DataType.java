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
