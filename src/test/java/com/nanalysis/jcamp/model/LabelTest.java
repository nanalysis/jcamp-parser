package com.nanalysis.jcamp.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LabelTest {
    @Test
    public void testLabelNormalization() {
        assertEquals("JCAMPDX", Label.JCAMP_DX.normalized());
        assertEquals("DATATYPE", Label.DATA_TYPE.normalized());
    }
}
