package cloud.quinimbus.persistence.test.base;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.entity.EmbeddedObject;
import cloud.quinimbus.persistence.api.schema.InvalidSchemaException;
import cloud.quinimbus.persistence.api.storage.PersistenceStorageProvider;
import cloud.quinimbus.persistence.common.filter.FilterFactory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.LogManager;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class AbstractStorageProviderTest {

    private PersistenceContext persistenceContext;

    public abstract PersistenceStorageProvider getStorageProvider();

    public abstract Map<String, Object> getParams();

    @BeforeEach
    public void init() throws IOException {
        LogManager.getLogManager().readConfiguration(AbstractStorageProviderTest.class.getResourceAsStream("logging.properties"));
        this.persistenceContext = ServiceLoader.load(PersistenceContext.class).findFirst().get();
    }

    @Test
    public void testInitSchema() throws IOException, InvalidSchemaException {
        this.persistenceContext.importSchemaFromSingleJson(new InputStreamReader(AbstractStorageProviderTest.class.getResourceAsStream("AbstractStorageProviderTest_schema.json"), Charset.forName("UTF-8")));
    }
    
    @Test
    public void testMetadata() throws IOException, PersistenceException, InvalidSchemaException {
        var schema = this.persistenceContext.importSchemaFromSingleJson(new InputStreamReader(AbstractStorageProviderTest.class.getResourceAsStream("AbstractStorageProviderTest_schema.json"), Charset.forName("UTF-8")));
        var params = new LinkedHashMap<>(this.getParams());
        params.put("schema", schema.id());
        var storage = this.getStorageProvider().createSchema(this.persistenceContext, params);
        var metadata = storage.getSchemaMetadata();
        Assertions.assertEquals("blog", metadata.id());
        Assertions.assertEquals(1, metadata.version());
    }
    
    @Test
    public void testMigration() throws IOException, PersistenceException, InvalidSchemaException {
        var schema = this.persistenceContext.importSchemaFromSingleJson(new InputStreamReader(AbstractStorageProviderTest.class.getResourceAsStream("AbstractStorageProviderTest_schema.json"), Charset.forName("UTF-8")));
        var params = new LinkedHashMap<>(this.getParams());
        params.put("schema", schema.id());
        var storage = this.getStorageProvider().createSchema(this.persistenceContext, params);
        
        var entryType = schema.entityTypes().get("entry");
        var authorType = entryType.embeddedPropertyType("author").orElseThrow();
        var commentType = entryType.embeddedPropertyType("comments").orElseThrow();
        var entry = this.persistenceContext.newEntity("first", entryType);
        entry.setProperty("title", "My first entry");
        var author = this.persistenceContext.newEmbedded(
                authorType,
                entryType,
                List.of("author"),
                Map.of("name", "John Doe", "subtext", "The most average guy"));
        entry.setProperty("author", author);
        entry.setProperty("comments", List.of(
                this.persistenceContext.newEmbedded(
                        commentType,
                        entryType,
                        List.of("comments"),
                        Map.of("text", "comment 1")),
                this.persistenceContext.newEmbedded(
                        commentType,
                        entryType,
                        List.of("comments"),
                        Map.of("text", "comment 1"))));
        storage.save(entry);
        
        schema = this.persistenceContext.importSchemaFromSingleJson(new InputStreamReader(AbstractStorageProviderTest.class.getResourceAsStream("AbstractStorageProviderTest_schema_migration.json"), Charset.forName("UTF-8")));
        this.persistenceContext.upgradeSchema(storage);
        var metadata = storage.getSchemaMetadata();
        Assertions.assertEquals(2, metadata.version());
        Assertions.assertEquals(3, metadata.entityTypeMigrationRuns().size());
        
        entryType = schema.entityTypes().get("entry");
        entry = storage.find(entryType, "first").orElseThrow();
        Assertions.assertEquals("no sponsor", entry.getProperty("sponsor"));
        Assertions.assertEquals("STAFF", entry.<EmbeddedObject>getProperty("author").getProperty("role"));
        Assertions.assertEquals(Instant.parse("2023-02-16T00:00:00Z"), entry.<List<EmbeddedObject>>getProperty("comments").get(0).getProperty("posted"));
    }

    @Test
    public void testSaveAndLoad() throws IOException, PersistenceException, InvalidSchemaException {
        var schema = this.persistenceContext.importSchemaFromSingleJson(new InputStreamReader(AbstractStorageProviderTest.class.getResourceAsStream("AbstractStorageProviderTest_schema.json"), Charset.forName("UTF-8")));
        var params = new LinkedHashMap<>(this.getParams());
        params.put("schema", schema.id());
        var storage = this.getStorageProvider().createSchema(this.persistenceContext, params);
        var entryType = schema.entityTypes().get("entry");
        var authorType = entryType.embeddedPropertyType("author").orElseThrow();
        var commentType = entryType.embeddedPropertyType("comments").orElseThrow();
        var firstEntry = this.persistenceContext.newEntity("first", entryType);
        firstEntry.setProperty("title", "My first entry");
        firstEntry.setProperty("published", true);
        firstEntry.setProperty("publishDate", LocalDate.now());
        firstEntry.setProperty("created", Instant.now().truncatedTo(ChronoUnit.MILLIS));
        firstEntry.setProperty("category", "POLITICS");
        firstEntry.setProperty("readcount", 15);
        firstEntry.setProperty("tags", List.of("election", "politics"));
        firstEntry.setProperty("author", this.persistenceContext.newEmbedded(
                authorType,
                entryType,
                List.of("author"),
                Map.of(
                        "name", "Max Mustermann",
                        "subtext", "The first of all authors.")));
        firstEntry.setProperty("ratings", Map.of(
                "userA", 1, "userB", 2));
        firstEntry.setProperty("comments", List.of(
                this.persistenceContext.newEmbedded(
                        commentType,
                        entryType,
                        List.of("comments"),
                        Map.of("text", "comment 1")),
                this.persistenceContext.newEmbedded(
                        commentType,
                        entryType,
                        List.of("comments"),
                        Map.of("text", "comment 1"))));
        storage.save(firstEntry);
        var resultFromStorage = storage.find(entryType, "first").get();
        Assertions.assertEquals(firstEntry, resultFromStorage);
    }

    @Test
    public void testSaveAndLoadList() throws IOException, PersistenceException, InvalidSchemaException {
        var schema = this.persistenceContext.importSchemaFromSingleJson(new InputStreamReader(AbstractStorageProviderTest.class.getResourceAsStream("AbstractStorageProviderTest_schema.json"), Charset.forName("UTF-8")));
        var params = new LinkedHashMap<>(this.getParams());
        params.put("schema", schema.id());
        var storage = this.getStorageProvider().createSchema(this.persistenceContext, params);
        var entryType = schema.entityTypes().get("entry");
        var firstEntry = this.persistenceContext.newEntity("first", entryType);
        firstEntry.setProperty("title", "My first entry");
        firstEntry.setProperty("published", true);
        firstEntry.setProperty("publishDate", LocalDate.now());
        firstEntry.setProperty("created", Instant.now().truncatedTo(ChronoUnit.MILLIS));
        firstEntry.setProperty("category", "POLITICS");
        firstEntry.setProperty("readcount", 15);
        firstEntry.setProperty("tags", List.of("election", "politics"));
        firstEntry.setProperty("ratings", Map.of(
                "userA", 1, "userB", 2));
        //firstEntry.setProperty("author", Map.of("name", "Max Mustermann", "subtext", "The first of all authors."));
        storage.save(firstEntry);
        var secondEntry = this.persistenceContext.newEntity("second", entryType);
        secondEntry.setProperty("title", "My second entry");
        secondEntry.setProperty("published", true);
        secondEntry.setProperty("publishDate", LocalDate.now());
        secondEntry.setProperty("created", Instant.now().truncatedTo(ChronoUnit.MILLIS));
        secondEntry.setProperty("category", "POLITICS");
        secondEntry.setProperty("readcount", 12);
        secondEntry.setProperty("tags", List.of("election", "politics"));
        secondEntry.setProperty("ratings", Map.of(
                "userA", 5, "userB", 1));
        //secondEntry.setProperty("author", Map.of("name", "Max Mustermann", "subtext", "The first of all authors."));
        storage.save(secondEntry);
        var resultFromStorage = storage.findAll(entryType).collect(Collectors.toList());
        Assertions.assertEquals(firstEntry, resultFromStorage.get(0));
        Assertions.assertEquals(secondEntry, resultFromStorage.get(1));
    }

    @Test
    public void testSaveAndLoadFiltered() throws IOException, PersistenceException, InvalidSchemaException {
        var schema = this.persistenceContext.importSchemaFromSingleJson(new InputStreamReader(AbstractStorageProviderTest.class.getResourceAsStream("AbstractStorageProviderTest_schema.json"), Charset.forName("UTF-8")));
        var params = new LinkedHashMap<>(this.getParams());
        params.put("schema", schema.id());
        var storage = this.getStorageProvider().createSchema(this.persistenceContext, params);
        var entryType = schema.entityTypes().get("entry");
        var firstEntry = this.persistenceContext.newEntity("first", entryType);
        firstEntry.setProperty("title", "My first entry");
        firstEntry.setProperty("published", true);
        firstEntry.setProperty("publishDate", LocalDate.now());
        firstEntry.setProperty("created", Instant.now().truncatedTo(ChronoUnit.MILLIS));
        firstEntry.setProperty("category", "POLITICS");
        firstEntry.setProperty("readcount", 15);
        firstEntry.setProperty("tags", List.of("election", "politics"));
        //firstEntry.setProperty("author", Map.of("name", "Max Mustermann", "subtext", "The first of all authors."));
        storage.save(firstEntry);
        var secondEntry = this.persistenceContext.newEntity("second", entryType);
        secondEntry.setProperty("title", "My second entry");
        secondEntry.setProperty("published", true);
        secondEntry.setProperty("publishDate", LocalDate.now());
        secondEntry.setProperty("created", Instant.now().truncatedTo(ChronoUnit.MILLIS));
        secondEntry.setProperty("category", "SPORTS");
        secondEntry.setProperty("readcount", 12);
        secondEntry.setProperty("tags", List.of("soccer", "championship"));
        //secondEntry.setProperty("author", Map.of("name", "Max Mustermann", "subtext", "The first of all authors."));
        storage.save(secondEntry);
        var sportsEntries = storage.findFiltered(entryType, FilterFactory.fromMap(Map.of("category", "SPORTS"))).collect(Collectors.toList());
        Assertions.assertEquals(1, sportsEntries.size());
        Assertions.assertEquals(secondEntry, sportsEntries.get(0));
    }
}
