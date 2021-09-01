package rs2d.jcamp.model;

import static org.junit.Assert.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

public class JCampContainerTest {
    private JCampContainer container;

    @Before
    public void setup() {
        container = new JCampContainer() {}; // inline subclass to test abstract class
    }

    @Test
    public void contains() {
        container.addRecord(new JCampRecord(Label.OWNER.name(), "TEST"));
        container.addRecord(new JCampRecord("not normalized", "other"));

        assertTrue(container.contains(Label.OWNER));
        assertTrue(container.contains("OWNER"));
        assertTrue(container.contains("not normalized"));
        assertTrue(container.contains("NOTNORMALIZED"));
        assertTrue(container.contains("NOT-NORMALIZED"));
        assertFalse(container.contains(Label.NPOINTS));
        assertFalse(container.contains("OTHER"));
    }

    @Test
    public void list() {
        container.addRecord(new JCampRecord(Label.OWNER.name(), "FIRST"));
        container.addRecord(new JCampRecord(Label.OWNER.name(), "SECOND"));
        container.addRecord(new JCampRecord(Label.DATA_TYPE.name(), "single"));

        assertEquals(List.of("FIRST", "SECOND"),
            container.list(Label.OWNER).stream().map(JCampRecord::getString).collect(Collectors.toList()));
        assertEquals(List.of("single"),
            container.list("DATA TYPE").stream().map(JCampRecord::getString).collect(Collectors.toList()));
        assertTrue(container.list("OTHER").isEmpty());
    }

    @Test
    public void optional() {
        container.addRecord(new JCampRecord(Label.OWNER.name(), "TEST"));

        assertTrue(container.optional(Label.OWNER).isPresent());
        assertTrue(container.optional("owner").isPresent());
        assertEquals("TEST", container.optional(Label.OWNER).map(JCampRecord::getString).orElse("not found"));

        assertFalse(container.optional("UNKNOWN").isPresent());
    }

    @Test
    public void optionalMultiLabels() {
        container.addRecord(new JCampRecord(Label.OWNER.name(), "owner"));
        container.addRecord(new JCampRecord(Label.ORIGIN.name(), "origin"));

        assertEquals("owner", container.optional(Label.OWNER, Label.ORIGIN).map(JCampRecord::getString).orElse("not found"));
        assertEquals("origin", container.optional(Label.ORIGIN, Label.OWNER).map(JCampRecord::getString).orElse("not found"));
        assertEquals("owner", container.optional(Label.DATA_TYPE, Label.OWNER, Label.ORIGIN).map(JCampRecord::getString).orElse("not found"));
        assertFalse(container.optional(Label.DATA_TYPE, Label.DATA_CLASS).isPresent());

        assertEquals("owner", container.optional("OWNER", "ORIGIN").map(JCampRecord::getString).orElse("not found"));
        assertEquals("origin", container.optional("origin", "owner").map(JCampRecord::getString).orElse("not found"));
        assertEquals("owner", container.optional("unknown", "OWNER", "ORIGIN").map(JCampRecord::getString).orElse("not found"));
        assertFalse(container.optional("unknown", "other").isPresent());
    }

    @Test
    public void optionalMultiRecord() {
        container.addRecord(new JCampRecord(Label.OWNER.name(), "FIRST"));
        container.addRecord(new JCampRecord(Label.OWNER.name(), "SECOND"));
        container.addRecord(new JCampRecord(Label.ORIGIN.name(), "SINGLE"));

        assertEquals("FIRST", container.optional(Label.OWNER, 0).map(JCampRecord::getString).orElse("not found"));
        assertEquals("SECOND", container.optional(Label.OWNER, 1).map(JCampRecord::getString).orElse("not found"));
        assertEquals("SINGLE", container.optional(Label.ORIGIN, 0).map(JCampRecord::getString).orElse("not found"));
        assertFalse(container.optional(Label.ORIGIN, 1).isPresent());
        assertFalse(container.optional(Label.DATA_CLASS, 0).isPresent());

        assertEquals("FIRST", container.optional("Owner", 0).map(JCampRecord::getString).orElse("not found"));
        assertEquals("SECOND", container.optional("Owner", 1).map(JCampRecord::getString).orElse("not found"));
        assertEquals("SINGLE", container.optional("Origin", 0).map(JCampRecord::getString).orElse("not found"));
        assertFalse(container.optional("ORIGIN", 1).isPresent());
        assertFalse(container.optional("UNKNOWN", 0).isPresent());
    }

    @Test(expected = NoSuchElementException.class)
    public void getUnknownLabel() {
        container.get(Label.DATA_CLASS);
    }

    @Test(expected = NoSuchElementException.class)
    public void getUnknownStringLabel() {
        container.get("unknown");
    }

    @Test
    public void get() {
        container.addRecord(new JCampRecord(Label.OWNER.name(), "FIRST"));
        container.addRecord(new JCampRecord(Label.OWNER.name(), "SECOND"));

        assertEquals("FIRST", container.get(Label.OWNER).getString());
        assertEquals("FIRST", container.get("owner").getString());
    }

    @Test
    public void getMultiRecord() {
        container.addRecord(new JCampRecord(Label.OWNER.name(), "FIRST"));
        container.addRecord(new JCampRecord(Label.OWNER.name(), "SECOND"));

        assertEquals("FIRST", container.get(Label.OWNER, 0).getString());
        assertEquals("SECOND", container.get(Label.OWNER, 1).getString());
        assertEquals("FIRST", container.get("owner", 0).getString());
        assertEquals("SECOND", container.get("owner", 1).getString());
    }

    @Test(expected = NoSuchElementException.class)
    public void getMultiRecordWithBadIndex() {
        container.addRecord(new JCampRecord(Label.OWNER.name(), "SINGLE"));
        container.get(Label.OWNER, 1);
    }

    @Test
    public void getOrDefault() {
        container.addRecord(new JCampRecord(Label.OWNER.name(), "FIRST"));
        container.addRecord(new JCampRecord(Label.OWNER.name(), "SECOND"));
        container.addRecord(new JCampRecord(Label.ORIGIN.name(), "SINGLE"));

        assertEquals("FIRST", container.getOrDefault(Label.OWNER, "DEFAULT").getString());
        assertEquals("SINGLE", container.getOrDefault(Label.ORIGIN, "DEFAULT").getString());
        assertEquals("DEFAULT", container.getOrDefault(Label.DATA_CLASS, "DEFAULT").getString());
        assertEquals("FIRST", container.getOrDefault(Label.OWNER, 0, "DEFAULT").getString());

        assertEquals("FIRST", container.getOrDefault(Label.OWNER, 0, "DEFAULT").getString());
        assertEquals("SECOND", container.getOrDefault(Label.OWNER, 1, "DEFAULT").getString());
        assertEquals("DEFAULT", container.getOrDefault(Label.OWNER, 2, "DEFAULT").getString());

        assertEquals("FIRST", container.getOrDefault("owner", "DEFAULT").getString());
        assertEquals("SINGLE", container.getOrDefault("origin", "DEFAULT").getString());
        assertEquals("DEFAULT", container.getOrDefault("unknown", "DEFAULT").getString());

        assertEquals("FIRST", container.getOrDefault("owner", 0, "DEFAULT").getString());
        assertEquals("SECOND", container.getOrDefault("owner", 1, "DEFAULT").getString());
        assertEquals("DEFAULT", container.getOrDefault("owner", 2, "DEFAULT").getString());
    }
}
