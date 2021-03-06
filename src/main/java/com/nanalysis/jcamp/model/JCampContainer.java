/*
 * JCamp-Parser: a basic parsing library
 * Copyright (C) 2021 - Nanalysis Scientific Corp.
 * -
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.nanalysis.jcamp.model;

import static com.nanalysis.jcamp.util.JCampUtil.normalize;

import java.util.*;

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
     * Get all record keys. Used to know which keys are defined in this container.
     * @return a non-modifiable set of all defined (normalized) keys.
     */
    public Set<String> allRecordKeys() {
        return Collections.unmodifiableSet(records.keySet());
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
    public List<JCampRecord> list(Label label) {
        return list(label.normalized());
    }

    /**
     * @param label any record label
     * @return a list of records for this label, or an empty list if no record are stored for this label.
     */
    public List<JCampRecord> list(String label) {
        return records.getOrDefault(normalize(label), Collections.emptyList());
    }

    /**
     * @param label a well known record label
     * @return the first record found, or empty if none match.
     */
    public Optional<JCampRecord> optional(Label label) {
        return optional(label.normalized());
    }

    /**
     * @param label any record label
     * @return the first record found, or empty if none match.
     */
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
    public JCampRecord get(Label label) {
        return get(label.normalized());
    }

    /**
     * @param label any record label
     * @return the first record found for this label.
     * @throws NoSuchElementException when there is no record for this label.
     */
    public JCampRecord get(String label) {
        return get(label, 0);
    }

    /**
     * Search for a single label, expecting a list result. Extract the indexed item from this list.
     *
     * @param label a well known record label
     * @param index the index of the record to get (zero-based)
     * @return the record found for this label and index.
     * @throws NoSuchElementException when there is fewer records than expected for this label.
     */
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
    public JCampRecord getOrDefault(String label, int index, String defaultData) {
        return optional(label, index).orElseGet(() -> new JCampRecord(label, defaultData));
    }
}
