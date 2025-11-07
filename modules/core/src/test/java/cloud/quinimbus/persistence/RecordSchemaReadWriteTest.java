package cloud.quinimbus.persistence;

import static org.junit.jupiter.api.Assertions.*;

import cloud.quinimbus.persistence.api.annotation.Embeddable;
import cloud.quinimbus.persistence.api.annotation.Entity;
import cloud.quinimbus.persistence.api.annotation.EntityField;
import cloud.quinimbus.persistence.api.annotation.EntityIdField;
import cloud.quinimbus.persistence.api.annotation.EntityTransientField;
import cloud.quinimbus.persistence.api.annotation.GenerateID;
import cloud.quinimbus.persistence.api.annotation.Schema;
import cloud.quinimbus.persistence.api.entity.EntityReaderInitialisationException;
import cloud.quinimbus.persistence.api.entity.EntityWriterInitialisationException;
import cloud.quinimbus.persistence.api.schema.InvalidSchemaException;
import cloud.quinimbus.persistence.entity.reader.RecordEntityReader;
import cloud.quinimbus.persistence.entity.writer.RecordEntityWriter;
import cloud.quinimbus.persistence.schema.record.RecordSchemaProvider;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RecordSchemaReadWriteTest {

    public static enum Category {
        POLITICS,
        SPORTS
    }

    @Embeddable
    public static record Author(String name, String subtext) {}

    @Embeddable
    public static record Ad(String company) {}

    public static record BlogEntry(
            @EntityIdField String id,
            String title,
            Boolean published,
            Instant created,
            Category category,
            Integer readcount,
            @EntityField(type = String.class) Set<String> tags,
            Author author,
            @EntityField(type = Ad.class) Set<Ad> ads,
            @EntityField(type = Integer.class) Map<String, Integer> ratings) {}

    public static record EntityWithTransientField(
            @EntityIdField String id,
            String includedField,
            @EntityTransientField AtomicBoolean ignoredField) {}

    @Embeddable
    public static record EmbeddedEntityWithTransientField(
            String includedField, @EntityTransientField AtomicBoolean ignoredField) {}

    public static record EntityWithEmbeddedTransientField(
            @EntityIdField String id, EmbeddedEntityWithTransientField embedded) {}

    @Entity(schema = @Schema(id = "generate-id-test", version = 1L))
    public static record EntityWithGeneratedId(
            @EntityIdField(generate = @GenerateID(generate = true, generator = "uuid"))
            String id) {}

    @Test
    public void testReaderAndWriter()
            throws InvalidSchemaException, EntityReaderInitialisationException, EntityWriterInitialisationException {
        var schemaProvider = new RecordSchemaProvider();
        var schema = schemaProvider.importSchema("blog", 1L, BlogEntry.class);
        var blogEntryType = schema.entityTypes().get("blogEntry");
        var blogEntryReader =
                new RecordEntityReader<>(new PersistenceContextImpl(), blogEntryType, BlogEntry.class, "id");
        var blogEntryWriter = new RecordEntityWriter<>(blogEntryType, BlogEntry.class, "id");
        var entry = new BlogEntry(
                "first",
                "My first entry",
                true,
                Instant.parse("2021-09-01T00:00:00Z"),
                Category.POLITICS,
                15,
                Set.of("europe", "politics"),
                new Author("Max Mustermann", "The first of all authors."),
                Set.of(new Ad("Google")),
                Map.of("userA", 1, "userB", 3));
        var entity = blogEntryReader.read(entry);
        var map = entity.asBasicMap();
        Assertions.assertTrue(map.get("author") instanceof Map);
        Assertions.assertTrue(
                map.get("ads") instanceof Set s && s.stream().findFirst().get() instanceof Map);
        Assertions.assertEquals("POLITICS", map.get("category"));
        var loaded = blogEntryWriter.write(entity);
        Assertions.assertEquals(entry, loaded);
    }

    @Test
    public void testNullValues()
            throws InvalidSchemaException, EntityReaderInitialisationException, EntityWriterInitialisationException {
        var schemaProvider = new RecordSchemaProvider();
        var schema = schemaProvider.importSchema("blog", 1L, BlogEntry.class);
        var blogEntryType = schema.entityTypes().get("blogEntry");
        var blogEntryReader =
                new RecordEntityReader<>(new PersistenceContextImpl(), blogEntryType, BlogEntry.class, "id");
        var blogEntryWriter = new RecordEntityWriter<>(blogEntryType, BlogEntry.class, "id");
        var entry = new BlogEntry(
                "null",
                null,
                true,
                Instant.parse("2021-09-01T00:00:00Z"),
                null,
                15,
                Set.of("europe", "politics"),
                new Author("Max Mustermann", "The first of all authors."),
                Set.of(new Ad("Google")),
                Map.of());
        var entity = blogEntryReader.read(entry);
        var loaded = blogEntryWriter.write(entity);
        Assertions.assertEquals(entry, loaded);
    }

    @Test
    public void testTransientFields()
            throws InvalidSchemaException, EntityReaderInitialisationException, EntityWriterInitialisationException {
        var schemaProvider = new RecordSchemaProvider();
        var schema = schemaProvider.importSchema("ignore-test", 1L, EntityWithTransientField.class);
        var entityType = schema.entityTypes().get("entityWithTransientField");
        assertEquals(1, entityType.properties().size());
        var entityReader = new RecordEntityReader<>(
                new PersistenceContextImpl(), entityType, EntityWithTransientField.class, "id");
        var entityWriter = new RecordEntityWriter<>(entityType, EntityWithTransientField.class, "id");
        var entityRecord = new EntityWithTransientField("1", "Hello", new AtomicBoolean(false));
        var entity = entityReader.read(entityRecord);
        entity.clearTransientFields();
        var loaded = entityWriter.write(entity);
        assertEquals(entityRecord.id(), loaded.id());
        assertEquals(entityRecord.includedField(), loaded.includedField());
        assertNull(loaded.ignoredField());
    }

    @Test
    public void testEmbeddedTransientFields()
            throws InvalidSchemaException, EntityReaderInitialisationException, EntityWriterInitialisationException {
        var schemaProvider = new RecordSchemaProvider();
        var schema = schemaProvider.importSchema("ignore-test", 1L, EntityWithEmbeddedTransientField.class);
        var entityType = schema.entityTypes().get("entityWithEmbeddedTransientField");
        var entityReader = new RecordEntityReader<>(
                new PersistenceContextImpl(), entityType, EntityWithEmbeddedTransientField.class, "id");
        var entityWriter = new RecordEntityWriter<>(entityType, EntityWithEmbeddedTransientField.class, "id");
        var entityRecord = new EntityWithEmbeddedTransientField(
                "1", new EmbeddedEntityWithTransientField("Hello", new AtomicBoolean(false)));
        var entity = entityReader.read(entityRecord);
        entity.clearTransientFields();
        var loaded = entityWriter.write(entity);
        assertEquals(entityRecord.id(), loaded.id());
        assertEquals(entityRecord.embedded().includedField(), loaded.embedded().includedField());
        assertNull(loaded.embedded().ignoredField());
    }

    @Test
    public void testGenerateID() throws InvalidSchemaException {
        var persistenceContext = new PersistenceContextImpl();
        var schema = persistenceContext.importRecordSchema(EntityWithGeneratedId.class);
        var entity = persistenceContext.newEntity(null, schema.entityTypes().get("entityWithGeneratedId"));
        assertNotNull(entity.getId());
        assertTrue(((String) entity.getId()).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }
}
