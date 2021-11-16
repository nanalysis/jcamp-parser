package rs2d.jcamp.util;

import lombok.NonNull;

public class JCampUtil {
    private JCampUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * JCamp-DX entry labels (and even values) are not always written the same way.
     * This removed all whitespace, underscores, dashes, and converts to uppercase, for easy comparisons.
     *
     * @param s a string
     * @return an uppercase string without whitespace, underscore, or dash.
     */
    @NonNull
    public static String normalize(String s) {
        if (s == null) {
            return "";
        }

        return s.toUpperCase()
            .replace(" ", "")
            .replace("\t", "")
            .replace("_", "")
            .replace("-", "")
            .replace("/", "");
    }

    /**
     * Sometimes, nuclei names are written as "^1H" or "<1H>", when spinlab expects only "1H".
     *
     * @param nucleus a nucleus name from jcamp
     * @return a nucleus name without unwanted characters.
     */
    @NonNull
    public static String toNucleusName(String nucleus) {
        if (nucleus == null) {
            return "";
        }

        return nucleus.replace("^", "").replace("<", "").replace(">", "");
    }

    /**
     * Removes parenthesis from a string.
     *
     * @param data the input data
     * @return the data without parenthesis.
     */
    @NonNull
    public static String withoutParenthesis(String data) {
        if (data == null) {
            return "";
        }

        return data.replace("(", "").replace(")", "");
    }

    /**
     * Removes brackets from a string.
     *
     * @param data the input data
     * @return the data without brackets.
     */
    @NonNull
    public static String withoutBrackets(String data) {
        if (data == null) {
            return "";
        }

        return data.replace("[", "").replace("]", "");
    }
}
