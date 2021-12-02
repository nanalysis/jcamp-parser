package com.nanalysis.jcamp.model;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

/**
 * A JCamp document. This represents a JCamp-DX file using a tree like structure.
 * A document contains a few root-level records and one or more blocks.
 * In turn, each block contains several records, and one or more page. The pages contain the actual data points.
 * <p>
 * This was designed with compound files (data type "LINK") and multi-dimensional datasets in mind.
 * When the file isn't a compound file, there is no explicit block in the file.
 * This implementation hides this, and considers these files as documents with a single block.
 * <p>
 * The same idea is done for pages (data class "NTUPLES").
 * Some files don't contain explicit pages, but expose the XYDATA as a direct record.
 * In this case, the XYDATA is shown as if it was a single page.
 */
public class JCampDocument extends JCampContainer {
    private final List<JCampBlock> blocks = new ArrayList<>();

    /**
     * @return the document title.
     */
    public String getTitle() {
        return getOrDefault(Label.TITLE, "No title").getString();
    }

    /**
     * @return the JCamp-DX version used for this document.
     */
    public String getVersion() {
        return getOrDefault(Label.JCAMP_DX, "Undefined").getString();
    }

    /**
     * @return the data type defined by this document. Defaults to UNKNOWN when not defined.
     */
    public DataType getDataType() {
        return DataType.fromString(getOrDefault(Label.DATA_TYPE, "").getString());
    }

    /**
     * The number of blocks this document is supposed to contain. This may differ from the actual content if case of
     * an incomplete document, or during parsing. For documents that don't declare BLOCKS, a value of 1 is returned to
     * avoid data access difference between document types.
     *
     * @return the number of blocks declared by this document or 1 if none is declared.
     */
    public int getBlockCount() {
        return getOrDefault(Label.BLOCKS, "1").getInt();
    }

    /**
     * Add a block to this document.
     *
     * @param block the block to add
     */
    public void addBlock(JCampBlock block) {
        this.blocks.add(block);
    }

    /**
     * Check whether the number of contained blocks matches with the number of declared blocks.
     * When they don't, it means the document is either invalid or still being build.
     *
     * @return true when the number of blocks matches the BLOCKS declaration.
     */
    public boolean containsDeclaredNumberOfBlocks() {
        return blocks.size() == getBlockCount();
    }

    /**
     * Get a specific block from its index.
     *
     * @param index the index for the block
     * @return the specified block
     * @throws IndexOutOfBoundsException when there is no block for this index
     */
    public JCampBlock block(int index) {
        if (index < 0 || index >= blocks.size()) {
            throw new IndexOutOfBoundsException("No block at index: " + index);
        }

        return blocks.get(index);
    }

    /**
     * Iterate over blocks using streams.
     *
     * @return a stream of blocks.
     */
    public Stream<JCampBlock> blocks() {
        return blocks.stream();
    }

    /**
     * Find the first FID block, either single or multi-dimensional.
     *
     * @return the first FID block.
     * @throws NoSuchElementException when the document doesn't contain a FID block
     */
    public JCampBlock getFirstFid() {
        return blocks()
            .filter(b -> b.getDataType().isFID())
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("No FID block found in this document."));
    }

    /**
     * Find the first spectrum block, either single or multi-dimensional.
     *
     * @return the first spectrum block.
     * @throws NoSuchElementException when the document doesn't contain a spectrum block
     */
    public JCampBlock getFirstSpectrum() {
        return blocks()
            .filter(b -> b.getDataType().isSpectrum())
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("No SPECTRUM block found in this document."));
    }
}
