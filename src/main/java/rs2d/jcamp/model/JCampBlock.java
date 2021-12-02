package rs2d.jcamp.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A JCamp block, contained in a document.
 * A block contains several records, and one or more page. The pages contain the actual data points.
 * <p>
 * Some documents don't define blocks explicitly. This implementation tries to expose a single access method for both
 * compound (data type = LINK) and basic documents. When a basic document is parsed, a single block is added. In that
 * case, the block isn't expected to contain a title or version directly, but can return it from its parent document.
 * <p>
 * Some blocks don't use pages. This can be the case when the data class is "XYDATA". In that case, this implementation
 * exposes the XYDATA as if it was a page, so that callers have a single access mechanism.
 */
public class JCampBlock extends JCampContainer {
    private final JCampContainer parent;
    private final List<JCampPage> pages = new ArrayList<>();

    public JCampBlock(JCampContainer parent) {
        this.parent = parent;
    }

    /**
     * @return the block title.
     */
    public String getTitle() {
        return getOrDefaultRecursive(Label.TITLE, "No Title").getString();
    }

    /**
     * @return the JCamp-DX version used for this block.
     */
    public String getVersion() {
        return getOrDefaultRecursive(Label.JCAMP_DX, "Undefined").getString();
    }

    /**
     * @return the data type defined by this block. Defaults to UNKNOWN when not defined.
     */
    public DataType getDataType() {
        return DataType.fromString(getOrDefaultRecursive(Label.DATA_TYPE, "").getString());
    }

    /**
     * @return the data type defined by this block. Defaults to UNKNOWN when not defined.
     */
    public DataClass getDataClass() {
        return DataClass.fromString(getOrDefaultRecursive(Label.DATA_CLASS, "").getString());
    }

    /**
     * Uses LONG_DATE if possible, DATE with timestamp otherwise. Defaults to today if no date specified.
     *
     * @return the date when this block was acquired or generated.
     */
    public Date getDate() {
        try {
            return optional(Label.LONG_DATE, Label.$DATE).map(JCampRecord::getDate).orElseGet(Date::new);
        } catch (IllegalStateException e) {
            // not a date, return current date instead
            return new Date();
        }
    }

    /**
     * Add a page to this block
     *
     * @param page the page to add
     */
    public void addPage(JCampPage page) {
        pages.add(page);
    }

    /**
     * @return the number of pages contained in this block.
     */
    public int getPageCount() {
        return pages.size();
    }

    /**
     * Get a specific page from its index.
     *
     * @param index the index for the page
     * @return the specified page
     * @throws IndexOutOfBoundsException when there is no page for this index
     */
    public JCampPage page(int index) {
        if (index < 0 || index >= pages.size()) {
            throw new IndexOutOfBoundsException("No page at index: " + index);
        }

        return pages.get(index);
    }

    /**
     * Extract all pages having a Y symbol matching the argument. This is useful to select only real or imaginary data.
     *
     * @param symbol the Y symbol to look for
     * @return a new list containing only the matching pages.
     */
    public List<JCampPage> getPagesForYSymbol(String symbol) {
        return pages.stream().filter(p -> p.extractYSymbol().equals(symbol)).collect(Collectors.toList());
    }

    /**
     * Get a value from this block. If it isn't defined, tries to get it from its parent.
     * If it isn't defined by the parent either, then use a default value.
     *
     * @param label a well known label
     * @param defaultData the data to use in a volatile record when there is no record for this label
     * @return the first record found for this label, either in this block or in its parent, or a default one.
     */
    private JCampRecord getOrDefaultRecursive(Label label, String defaultData) {
        return optional(label)
            .orElseGet(() -> parent.getOrDefault(label, defaultData));
    }
}
