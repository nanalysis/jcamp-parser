package rs2d.jcamp.model;

import static rs2d.jcamp.util.JCampUtil.normalize;

import java.util.*;

import lombok.NonNull;

/**
 * Base container for JCamp Labelled Data Records, used to provide accessor methods to subclasses.
 * <p>
 * It can contain multiple records for the same label (used in multi-dimensional datasets),
 */
public abstract class JCampContainer {
    private final Map<String, List<JCampRecord>> records = new TreeMap<>();

    /**
     * Add a data record to this container.
     *
     * @param record a previously parsed record
     */
    public void addRecord(JCampRecord record) {
        List<JCampRecord> list = records.computeIfAbsent(record.getNormalizedLabel(), k -> new ArrayList<>());
        list.add(record);
    }

    /**
     * @param label a well known record label
     * @return true when the container has at least a record for this label.
     */
    public boolean contains(Label label) {
        return contains(label.normalized());
    }

    /**
     * @param label any record label
     * @return true when the container has at least a record for this label.
     */
    public boolean contains(String label) {
        return records.containsKey(normalize(label));
    }

    /**
     * @param label a well known record label
     * @return a list of records for this label, or an empty list if no record are stored for this label.
     */
    @NonNull
    public List<JCampRecord> list(Label label) {
        return list(label.normalized());
    }

    /**
     * @param label any record label
     * @return a list of records for this label, or an empty list if no record are stored for this label.
     */
    @NonNull
    public List<JCampRecord> list(String label) {
        return records.getOrDefault(normalize(label), Collections.emptyList());
    }

    /**
     * @param label a well known record label
     * @return the first record found, or empty if none match.
     */
    @NonNull
    public Optional<JCampRecord> optional(Label label) {
        return optional(label.normalized());
    }

    /**
     * @param label any record label
     * @return the first record found, or empty if none match.
     */
    @NonNull
    public Optional<JCampRecord> optional(String label) {
        return optional(label, 0);
    }

    /**
     * Search for several labels, return the first record found, respecting the label order.
     * This means that the second label will only be used if there is no record using the first one.
     * When several records are stored for the same label, only the first one is returned.
     *
     * @param labels two or more well known record labels
     * @return the first record found, or empty if none match.
     */
    @NonNull
    public Optional<JCampRecord> optional(Label... labels) {
        if (labels.length < 2) {
            throw new IllegalArgumentException("Expected two or more labels, received: " + labels.length);
        }

        return Arrays.stream(labels)
            .map(this::optional)
            .filter(Optional::isPresent)
            .findFirst().orElse(Optional.empty());
    }

    /**
     * Search for several labels, return the first record found, respecting the label order.
     * This means that the second label will only be used if there is no record using the first one.
     * When several records are stored for the same label, only the first one is returned.
     *
     * @param labels two or more record labels
     * @return the first record found, or empty if none match.
     */
    @NonNull
    public Optional<JCampRecord> optional(String... labels) {
        if (labels.length < 2) {
            throw new IllegalArgumentException("Expected two or more labels, received: " + labels.length);
        }

        return Arrays.stream(labels)
            .map(this::optional)
            .filter(Optional::isPresent)
            .findFirst().orElse(Optional.empty());
    }

    /**
     * Search for a single label, expecting a list result. Extract the indexed item from this list.
     * This is used for multi-dimensional datasets, when the same label can be set for two directions.
     * <p>
     * When the label is found, but there isn't enough items to return the requested one, this returns empty.
     *
     * @param label a well known record label
     * @param index the index of the record to get (zero-based)
     * @return the record found for this label and index, or empty if none match.
     */
    @NonNull
    public Optional<JCampRecord> optional(Label label, int index) {
        return optional(label.normalized(), index);
    }

    /**
     * Search for a single label, expecting a list result. Extract the indexed item from this list.
     * This is used for multi-dimensional datasets, when the same label can be set for two directions.
     * <p>
     * When the label is found, but there isn't enough items to return the requested one, this returns empty.
     *
     * @param label any record label
     * @param index the index of the record to get (zero-based)
     * @return the record found for this label and index, or empty if none match.
     */
    @NonNull
    public Optional<JCampRecord> optional(String label, int index) {
        List<JCampRecord> list = list(label);
        if (index < 0 || index >= list.size()) {
            return Optional.empty();
        }
        return Optional.of(list.get(index));
    }

    /**
     * @param label a well known record label
     * @return the first record found for this label.
     * @throws NoSuchElementException when there is no record for this label.
     */
    @NonNull
    public JCampRecord get(Label label) {
        return get(label.normalized());
    }

    /**
     * @param label any record label
     * @return the first record found for this label.
     * @throws NoSuchElementException when there is no record for this label.
     */
    @NonNull
    public JCampRecord get(String label) {
        return get(label, 0);
    }

    /**
     * Search for a single label, expecting a list result. Extract the indexed item from this list.
     *
     * @param label a well known record label
     * @param index the index of the record to get (zero-based)
     * @return the record found for this label and index.
     * @throws NoSuchElementException when there is less records than expected for this label.
     */
    @NonNull
    public JCampRecord get(Label label, int index) {
        return get(label.normalized(), index);
    }

    /**
     * Search for a single label, expecting a list result. Extract the indexed item from this list.
     *
     * @param label any record label
     * @param index the index of the record to get (zero-based)
     * @return the record found for this label and index.
     * @throws NoSuchElementException when there is less records than expected for this label.
     */
    public JCampRecord get(String label, int index) {
        List<JCampRecord> list = list(label);
        if (list.isEmpty()) {
            throw new NoSuchElementException("Undefined record: " + label);
        } else if (index < 0 || index >= list.size()) {
            throw new NoSuchElementException("Undefined record: " + label + " for index:" + index);
        }

        return list.get(index);
    }

    /**
     * Get a specific record for a label. When the record doesn't exist, create a volatile record using some default data.
     *
     * @param label a well known record label
     * @param defaultData the data to use in a volatile record when there is no record for this label
     * @return the first record found for this label or a default one.
     */
    @NonNull
    public JCampRecord getOrDefault(Label label, String defaultData) {
        return getOrDefault(label.normalized(), defaultData);
    }

    /**
     * Get a specific record for a label. When the record doesn't exist, create a volatile record using some default data.
     *
     * @param label any record label
     * @param defaultData the data to use in a volatile record when there is no record for this label
     * @return the first record found for this label or a default one.
     */
    @NonNull
    public JCampRecord getOrDefault(String label, String defaultData) {
        return getOrDefault(label, 0, defaultData);
    }

    /**
     * Search for a single label, expecting a list result. Extract the indexed item from this list.
     * When there is less records than expected for this label, create a volatile record using some default data.
     *
     * @param label a well known label
     * @param defaultData the data to use in a volatile record when there is no record for this label
     * @return the first record found for this label or a default one.
     */
    @NonNull
    public JCampRecord getOrDefault(Label label, int index, String defaultData) {
        return getOrDefault(label.normalized(), index, defaultData);
    }

    /**
     * Search for a single label, expecting a list result. Extract the indexed item from this list.
     * When there is less records than expected for this label, create a volatile record using some default data.
     *
     * @param label any record label
     * @param defaultData the data to use in a volatile record when there is no record for this label
     * @return the first record found for this label or a default one.
     */
    @NonNull
    public JCampRecord getOrDefault(String label, int index, String defaultData) {
        return optional(label, index).orElseGet(() -> new JCampRecord(label, defaultData));
    }
}
