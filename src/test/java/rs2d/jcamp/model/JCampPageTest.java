package rs2d.jcamp.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class JCampPageTest {
    private static final double DELTA = 1e-6;

    @Test
    public void extractSymbolsFromKnownPageDefinitions() {
        assertEquals("Should be able to parse without type information",
            List.of("X", "Y"), Arrays.asList(JCampPage.extractSymbols("(X++(Y..Y))")));
        assertEquals("Should be able to parse XYDATA",
            List.of("X", "R"), Arrays.asList(JCampPage.extractSymbols("(X++(R..R)), XYDATA")));
        assertEquals("Should be able to parse PROFILE",
            List.of("T2", "I"), Arrays.asList(JCampPage.extractSymbols("(T2++(I..I)), PROFILE")));
    }

    @Test
    public void extractVariableFor1dFid() {
        JCampPage page = new JCampPage(null);
        page.addRecord(new JCampRecord("DATA TABLE", "(X++(I..I)), XYDATA"));
        assertEquals("X", page.extractXSymbol());
        assertEquals("I", page.extractYSymbol());
    }

    @Test
    public void extractVariableFor2dFid() {
        JCampPage page = new JCampPage(null);
        page.addRecord(new JCampRecord("DATA TABLE", "(T2++(R..R)), PROFILE"));
        assertEquals("T2", page.extractXSymbol());
        assertEquals("R", page.extractYSymbol());
    }

    @Test
    public void extractValues() {
        JCampBlock parent = new JCampBlock(null);
        parent.addRecord(new JCampRecord(Label.SYMBOL.name(), "T, R, I"));
        parent.addRecord(new JCampRecord(Label.VAR_DIM.name(), "12, 12, 12"));

        JCampPage page = new JCampPage(parent);
        page.addRecord(new JCampRecord("DATA TABLE", "(T++(R..R)), XYDATA\n"
            + "0.00000000     -517905556    -2147482999     -901597641      179005696\n"
            + "16.00000000      725417901      218517690     -393853179     -665104755\n"
            + "32.00000000     -310803688      117493285      281062089       84565528\n"));

        double[] array = page.toArray();
        assertEquals(12, array.length);
        assertEquals(-517905556, (int) array[0]);
        assertEquals(-2147482999, (int) array[1]);
        assertEquals(725417901, (int) array[4]);
        assertEquals(84565528, (int) array[11]);
    }

    @Test
    public void extractValuesWithFactor() {
        JCampBlock parent = new JCampBlock(null);
        parent.addRecord(new JCampRecord(Label.SYMBOL.name(), "X,R,I,N"));
        parent.addRecord(new JCampRecord(Label.VAR_DIM.name(), "12, 12, 12, 2"));
        parent.addRecord(new JCampRecord(Label.FACTOR.name(), "0.00049940480000000001,0.00002428885732078813,0.00002428885732078813,1"));

        JCampPage page = new JCampPage(parent);
        page.addRecord(new JCampRecord("DATA TABLE", "(X++(R..R)), XYDATA\n"
            + "0.00000000     -517905556    -2147482999     -901597641      179005696\n"
            + "16.00000000      725417901      218517690     -393853179     -665104755\n"
            + "32.00000000     -310803688      117493285      281062089       84565528\n"));

        double[] array = page.toArray();
        assertEquals(12, array.length);
        assertEquals(-12579.334155327448, array[0], DELTA);
        assertEquals(-52159.9081615292, array[1], DELTA);
        assertEquals(17619.57189533461, array[4], DELTA);
        assertEquals(2054.000043849114, array[11], DELTA);
    }
}
