package rs2d.jcamp.model;

import static rs2d.jcamp.util.JCampUtil.normalize;

public enum Label {
    TITLE,
    JCAMP_DX,
    DATA_TYPE,
    DATA_CLASS,
    ORIGIN,
    OWNER,
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
    _AVERAGES,
    _PULSE_SEQUENCE,
    _SOLVENT_NAME,
    _OBSERVE_FREQUENCY,
    _OBSERVE_NUCLEUS,
    $BFREQ,
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
    $NUC_1,
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

        this.normalized = normalize(n);
    }

    public String normalized() {
        return normalized;
    }
}
