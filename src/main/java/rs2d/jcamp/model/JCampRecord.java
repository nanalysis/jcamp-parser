package rs2d.jcamp.model;

import static rs2d.jcamp.util.JCampUtil.normalize;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import lombok.NonNull;
import rs2d.jcamp.parser.JCampParser;
import rs2d.jcamp.util.JCampUtil;

/**
 * A record correspond to what the specification calls a "Labelled Data Record" (LDR).
 */
public class JCampRecord {
    private final int lineNumber;
    private final String label;
    private String data = "";
    private String comment = "";

    public JCampRecord(String label, String data) {
        this(-1, label, data);
    }

    private JCampRecord(int lineNumber, String label) {
        this(lineNumber, label, "");
    }

    private JCampRecord(int lineNumber, String label, String data) {
        this.lineNumber = lineNumber;
        this.label = label;
        this.data = data;
    }

    /**
     * The line where this record appeared in the source JCamp document.
     * For multi-line records, this is the line where the label appeared.
     *
     * @return the line number or -1 for volatile records.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * @return the record label as it appeared in the source JCamp document.
     */
    @NonNull
    public String getLabel() {
        return label;
    }

    /**
     * Get the record label in a normalized form. This can be useful for comparison or lookup of expected labels.
     *
     * @return the record label, in upper case, without whitespace, dash or underscore.
     */
    @NonNull
    public String getNormalizedLabel() {
        return normalize(label);
    }

    /**
     * @return the comment that was written on the same line as the record, or an empty string if no comment were present.
     */
    @NonNull
    public String getComment() {
        return comment;
    }

    /**
     * The record data is stored internally as a single string.
     * When the record contains a multi-line value, this is appended to the first line as a single string.
     *
     * @return the record data as it appeared in the source document, without any modification.
     */
    @NonNull
    public String getString() {
        return data;
    }

    /**
     * Splits a comma-separated record data. If the data contains no comma, a valid list list will be returned with a single element.
     *
     * @return the record data as a list of strings.
     */
    public List<String> getStrings() {
        return Arrays.stream(data.split(","))
            .map(String::trim)
            .collect(Collectors.toList());
    }

    /**
     * @return the record data as a single integer.
     */
    public int getInt() {
        return getInts()[0];
    }

    /**
     * Splits a comma-separated record data, and convert it to an array of integers.
     * Some record contains values in parenthesis, for example "(1, 2, 3)". In this case, the parenthesis will be stripped first.
     *
     * @return the record data as an integer array.
     */
    public int[] getInts() {
        return getStrings().stream()
            .map(JCampUtil::withoutParenthesis)
            .map(JCampUtil::withoutBrackets)
            .map(Double::valueOf) // passes through Double to accept poorly formatted values such as "0.0"
            .mapToInt(Number::intValue)
            .toArray();
    }

    /**
     * @return the record data as a single floating point number.
     */
    public double getDouble() {
        return getDoubles()[0];
    }

    /**
     * Splits a comma-separated record data, and convert it to an array of doubles.
     * Some record contains values in parenthesis, for example "(1.1, 2.1, 3.1)". In this case, the parenthesis will be stripped first.
     *
     * @return the record data as an double array.
     */
    public double[] getDoubles() {
        return getStrings().stream()
            .map(JCampUtil::withoutParenthesis)
            .map(JCampUtil::withoutBrackets)
            .mapToDouble(Double::parseDouble)
            .toArray();
    }

    /**
     * Parse the record data as a date. Supported formats include:
     * <ul>
     * <li>LONG DATE: 2021/12/09 15:54:27+0200</li>
     * <li>LONG DATE: 2021-12-09T15:54:27.887+02:00</li>
     * <li>LONG DATE: 2021/12/09</li>
     * <li>Timestamp: seconds since epoch</li>
     * </ul>
     * Formats without timezone are accepted as well, and will use the current locale.
     *
     * @return the date contained by this field.
     */
    public Date getDate() {
        List<String> patterns = List.of(
            "yyyy/MM/dd HH:mm:ssZ", // with timezone
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", // with milliseconds and timezone, using '-' and 'T' separators
            "yyyy/MM/dd HH:mm:ss.SSSZ", // with milliseconds and timezone
            "yyyy/MM/dd HH:mm:ss", // without timezone
            "yyyy-MM-dd'T'HH:mm:ss.SSS", // with milliseconds, using '-' and 'T' separators
            "yyyy/MM/dd HH:mm:ss.SSS", // with milliseconds
            "yyyy/MM/dd" // date only, without time information
        );
        for (String pattern : patterns) {
            try {
                return new SimpleDateFormat(pattern).parse(getString());
            } catch (ParseException e) {
                // not in this format, try next one
            }
        }

        // not a valid pattern, try with seconds since epoch
        try {
            return new Date(getInt() * 1000L);
        } catch (NumberFormatException e) {
            // not in this format either...
        }

        throw new IllegalStateException("Unable to parse date: " + getString());
    }

    /**
     * Parse a data line. This could be the right side of a single-lined Label-Data-Record (LDR) expression,
     * or an additional line in case of multiple-lined records.
     *
     * @param text some text to parse
     */
    public void parseData(String text) {
        String value = text.trim();
        String comment = "";
        if (value.contains(JCampParser.COMMENT_PREFIX)) {
            String[] valueWithComment = value.split("\\$\\$", 2);
            value = valueWithComment[0].trim();
            comment = valueWithComment[1].trim();
        }

        if (!this.data.isEmpty()) {
            this.data += "\n";
        }
        this.data += value;

        if (!this.comment.isEmpty() && !comment.isEmpty()) {
            this.comment += "\n";
        }
        this.comment += comment;
    }


    /**
     * Parse a labelled data record, with or without comment, without the "##" prefix.
     *
     * @param text a record expression
     * @return the parsed object
     */
    public static JCampRecord parse(String text) {
        return parse(-1, text);
    }

    /**
     * Parse a labelled data record, with or without comment, without the "##" prefix.
     *
     * @param lineNumber the line number, for debugging purposes
     * @param text a record expression
     * @return the parsed object
     */
    public static JCampRecord parse(int lineNumber, String text) {
        String[] expression = text.split("=", 2);
        if (expression.length != 2) {
            throw new IllegalArgumentException("Invalid Labelled Data Record text: " + text);
        }

        String label = expression[0].trim();
        JCampRecord record = new JCampRecord(lineNumber, label);
        record.parseData(expression[1]);
        return record;
    }
}
