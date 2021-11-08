package rs2d.jcamp.model;

import static rs2d.jcamp.util.JCampUtil.normalize;

public enum Label {
    TITLE,
    JCAMP_DX,
    DATA_TYPE,
    DATA_CLASS,
    ORIGIN,
    OWNER,
    LONG_DATE,
    PRESSURE,
    RESOLUTION,
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
    _OBSERVE_90,
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
    $SCANS,
    $TOTAL_DURATION,
    $BFREQ,
    $FID_RES,
    $TD,
    $DW,
    $DS,
    $GRPDLY,
    $DIGMOD,
    $DSPFVS,
    $NS,
    $PHC0,
    $PHC1,
    $PULPROG,
    $SOLVENT,
    $SW_H,
    $TE,
    $RG,
    $gain,
    $RECVR_GAIN,
    $RXGAIN,
    $SPECTRAL_WIDTH,
    $RELAXATIONDELAY,
    $NUC_1,
    $T1NUCLEUS,
    $NUC_2,
    $T2NUCLEUS,
    $BF1,
    $BF2,
    $SF,
    $O1,
    $O2,
    $SFO1,
    $SI;

    private final String normalized;

    Label() {
        String n = name();
        if (n.startsWith("_")) {
            // replace first '_' with '.' for private tags
            n = "." + n.substring(1);
        }

        this.normalized = normalize(n);
    }

    public String normalized() {
        return normalized;
    }
}
