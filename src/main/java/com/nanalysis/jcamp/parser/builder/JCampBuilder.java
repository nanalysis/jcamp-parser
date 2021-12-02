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
package com.nanalysis.jcamp.parser.builder;

import com.nanalysis.jcamp.model.JCampRecord;

/**
 * JCamp consumer: builds something out of JCamp records and comments.
 * Uses a hierarchy of builders to create a whole document.
 */
public interface JCampBuilder<T> {
    /**
     * Return the built (or still under construction) object
     *
     * @return the object being built.
     */
    T getObject();

    /**
     * Consumes a single entry, and return a builder that should be used for the next ones.
     * The returned builder can be this instance when the context doesn't change, a child for inner blocks, or the parent builder.
     * <p>
     * Note: this will be called as soon as a record is found. It may still be incomplete (for multi-line records).
     *
     * @param record a JCamp record to consume (or forward to another builder)
     * @return a builder to use for the following lines.
     */
    JCampBuilder<?> consume(JCampRecord record);

    /**
     * Consume a single comment line, and return a builder that should be used for the following content.
     * Default implementation ignores comments and returns the same builder instance.
     *
     * @param comment a comment line
     * @return a builder to use for the following lines.
     */
    default JCampBuilder<?> consumeComment(String comment) {
        return this;
    }
}
