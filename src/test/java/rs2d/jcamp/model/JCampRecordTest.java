package rs2d.jcamp.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class JCampRecordTest {
    private static final double DELTA = 1e-6;

    @Test(expected = IllegalArgumentException.class)
    public void parseInvalidRecord() {
        JCampRecord.parse("NO EQUALS SIGN");
    }

    @Test
    public void parseSingleValue() {
        String input = "BLOCKS=  2";
        JCampRecord parsed = JCampRecord.parse(input);

        assertEquals("BLOCKS", parsed.getLabel());
        assertEquals("2", parsed.getString());
        assertEquals(2, parsed.getInt());
        assertEquals("", parsed.getComment());
    }

    @Test
    public void parseDataWithComments() {
        String input = "JCAMPDX= 6.0         $$ Spinit NMR compound JCAMP-DX V2.0 - code from SPIKE by CASC4DE";
        JCampRecord parsed = JCampRecord.parse(input);

        assertEquals("JCAMPDX", parsed.getLabel());
        assertEquals("6.0", parsed.getString());
        assertEquals("Spinit NMR compound JCAMP-DX V2.0 - code from SPIKE by CASC4DE", parsed.getComment());
    }

    @Test
    public void parseMultiLine() {
        String input = "$RDC_PARAMETERS=(0,1,";
        List<String> newLines = List.of("2,3,", "4)");
        JCampRecord parsed = JCampRecord.parse(input);
        newLines.forEach(parsed::parseData);

        assertEquals("$RDC_PARAMETERS", parsed.getLabel());
        assertEquals("(0,1,\n2,3,\n4)", parsed.getString());
    }

    @Test
    public void parseMultiLineWithComments() {
        String input = "TEST=a $$ first";
        List<String> newLines = List.of("b $$ second", "c $$ third");
        JCampRecord parsed = JCampRecord.parse(input);
        newLines.forEach(parsed::parseData);

        assertEquals("TEST", parsed.getLabel());
        assertEquals("a\nb\nc", parsed.getString());
        assertEquals("first\nsecond\nthird", parsed.getComment());
    }

    @Test
    public void labelNormalization() {
        JCampRecord parsed = JCampRecord.parse("$.test-ME_now=");
        assertEquals("$.test-ME_now", parsed.getLabel());
        assertEquals("$.TESTMENOW", parsed.getNormalizedLabel());
    }

    @Test
    public void intConversion() {
        JCampRecord single = new JCampRecord("TEST", "42");
        JCampRecord floatingPoint = new JCampRecord("TEST", "4.2");
        JCampRecord multiple = new JCampRecord("TEST", "(5,6,\n 7,8)");
        assertEquals(42, single.getInt());
        assertEquals(4, floatingPoint.getInt());
        assertEquals(5, multiple.getInt());
        assertArrayEquals(new int[] {42}, single.getInts());
        assertArrayEquals(new int[] {5, 6, 7, 8}, multiple.getInts());
    }

    @Test
    public void doubleConversion() {
        JCampRecord single = new JCampRecord("TEST", "4.2");
        JCampRecord multiple = new JCampRecord("TEST", "(5.6,\n 7.8, 9.01e2)");
        assertEquals(4.2, single.getDouble(), DELTA);
        assertArrayEquals(new double[] {4.2}, single.getDoubles(), DELTA);
        assertArrayEquals(new double[] {5.6, 7.8, 901}, multiple.getDoubles(), DELTA);
    }
}
