package rs2d.jcamp.parser;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class ASDFParserTest {
    @Test
    public void tokenize() {
        String input = "0.0A5117J7650n9841e631K1245k9857l323L1191r423j4560F852q24k0491";
        assertEquals(
            List.of("0.0", "A5117", "J7650", "n9841", "e631", "K1245", "k9857", "l323", "L1191", "r423", "j4560", "F852", "q24", "k0491"),
            new ASDFParser(0).tokenize(input));
    }

    @Test
    public void tokenTypes() {
        ASDFParser parser = new ASDFParser(0);
        assertEquals(ASDFParser.TokenType.NUMERICAL, parser.tokenType("0.0"));
        assertEquals(ASDFParser.TokenType.SQZ, parser.tokenType("A5117"));
        assertEquals(ASDFParser.TokenType.SQZ, parser.tokenType("e631"));
        assertEquals(ASDFParser.TokenType.DIF, parser.tokenType("J7650"));
        assertEquals(ASDFParser.TokenType.DIF, parser.tokenType("j4560"));
        assertEquals(ASDFParser.TokenType.DUP, parser.tokenType("V"));
    }

    @Test
    public void tokenValues() {
        ASDFParser parser = new ASDFParser(0);
        assertEquals(0, parser.doubleValue("0.0"), 1e-6);
        assertEquals(15117, parser.intValue("A5117"));
        assertEquals(-5631, parser.intValue("e631"));
        assertEquals(17650, parser.intValue("J7650"));
        assertEquals(-14560, parser.intValue("j4560"));
        assertEquals(4, parser.intValue("V"));
    }

    @Test
    public void parseSqzDif() {
        String input = "0AJKkj";
        int[] expected = {1, 2, 4, 2, 1};

        ASDFParser parser = new ASDFParser(5);
        parser.parseSingleLine(input);
        assertArrayEquals(expected, parser.getData());
    }

    @Test
    public void parseSqzDifDup() {
        String input = "0AJV";
        int[] expected = {1, 2, 3, 4, 5};

        ASDFParser parser = new ASDFParser(5);
        parser.parseSingleLine(input);
        assertArrayEquals(expected, parser.getData());
    }

    @Test
    public void parseSqzDup() {
        String input = "0D2U";
        int[] expected = {42, 42, 42};

        ASDFParser parser = new ASDFParser(3);
        parser.parseSingleLine(input);
        assertArrayEquals(expected, parser.getData());
    }

    @Test
    public void afterDifWithYCheck() {
        // extract from spinit old export:
        // X matches index (but is in floating point...)
        // last Y value is repeated in second line
        List<String> input = List.of(
            "0.0A5117J7650n9841e631K1245k9857l323L1191r423j4560F852q24k0491",
            "12.0a4463R789J4864c014n359G324O7g802k56R792N45m859j802J745N070");

        ASDFParser parser = new ASDFParser(27);
        int[] result = parser.parse(input);
        assertEquals(27, result.length);
    }


    @Test
    public void afterDifWithoutYCheck() {
        // extract from spinit cascade export for topspin:
        // X matches index
        // Y value not repeated in second line even if first line finishes with a DIF token
        List<String> input = List.of(
            "0c0k343K035K087k983M58K501j680j408K267l09k200J379J662j605o05J529k10j380L29Q74",
            "21B41l21o2N8L26k4o26k8O08J05j05l3o55j16J241R5j285M96J037j284o98J583P2j213P90");

        ASDFParser parser = new ASDFParser(46);
        int[] result = parser.parse(input);
        assertEquals(46, result.length);
    }
}
