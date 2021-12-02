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
