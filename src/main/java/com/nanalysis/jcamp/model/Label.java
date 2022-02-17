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

import com.nanalysis.jcamp.util.JCampUtil;

public enum Label {
    TITLE,
    JCAMP_DX,
    DATA_TYPE,
    DATA_CLASS,
    ORIGIN,
    OWNER,
    LONG_DATE,
    SPECTROMETER_DATA_SYSTEM,
    PRESSURE,
    SAMPLE_DESCRIPTION,
    NPOINTS,
    NUMDIM,
    BLOCKS,
    PAGE,
    DATA_TABLE,
    XYDATA,
    SYMBOL,
    VAR_TYPE,
    VAR_FORM,
    VAR_DIM,
    UNITS,
    FIRST,
    LAST,
    FACTOR,
    TEMPERATURE,
    SPECTRAL_WIDTH,
    _SPECTRAL_WIDTH,
    _DELAY,
    _FIELD,
    _ACQUISITION_TIME,
    _LAST_TIME,
    _AVERAGES,
    _SPINNING_RATE,
    _PULSE_SEQUENCE,
    _SOLVENT_NAME,
    _OBSERVE_FREQUENCY,
    _OBSERVE_NUCLEUS,
    _ACQUISITION_SCHEME,
    _NUCLEUS,
    $ORIGINAL_FORMAT,
    $DATE,
    $SCAN_DELAY,
    $DE,
    $SCANS,
    $TOTAL_DURATION,
    $BFREQ,
    $FID_RES,
    $TD,
    $DW,
    $DS,
    $GRPDLY,
    $NS,
    $PHC0,
    $PHC1,
    $PULPROG,
    $SOLVENT,
    $SW_H,
    $TE,
    $RG,
    $GAIN,
    $RECVR_GAIN,
    $RXGAIN,
    $SPECTRAL_WIDTH,
    $RELAXATION_DELAY,
    $APPLICATION_PARAMETER_TAU,
    $NUC_1,
    $T2_NUCLEUS,
    $NUC_2,
    $BF1,
    $BF2,
    $SF,
    $O1,
    $O2,
    $SFO1;

    private final String normalized;

    Label() {
        String n = name();
        if (n.startsWith("_")) {
            // replace first '_' with '.' for private tags
            n = "." + n.substring(1);
        }

        this.normalized = JCampUtil.normalize(n);
    }

    public String normalized() {
        return normalized;
    }
}
