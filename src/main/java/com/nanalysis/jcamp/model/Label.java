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
