package rs2d.jcamp.parser.builder;

import rs2d.jcamp.model.JCampRecord;

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
