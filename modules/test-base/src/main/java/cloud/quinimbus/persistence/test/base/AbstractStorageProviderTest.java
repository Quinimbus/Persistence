package cloud.quinimbus.persistence.test.base;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.schema.properties.EmbeddedPropertyType;
import cloud.quinimbus.persistence.api.storage.PersistenceStorageProvider;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class AbstractStorageProviderTest {

    private PersistenceContext persistenceContext;
    
    public abstract PersistenceStorageProvider getStorageProvider();

    @BeforeEach
    public void init() {
        this.persistenceContext = ServiceLoader.load(PersistenceContext.class).findFirst().get();
    }
    
    @Test
    public void testInitSchema() throws IOException {
        this.persistenceContext.importSchemaFromSingleJson(AbstractStorageProviderTest.class.getResourceAsStream("AbstractStorageProviderTest_schema.json"));
    }
    
    @Test
    public void testSaveAndLoad() throws IOException, PersistenceException {
        var provider = this.persistenceContext.importSchemaFromSingleJson(AbstractStorageProviderTest.class.getResourceAsStream("AbstractStorageProviderTest_schema.json"));
        var schema = provider.getSchemas().stream().findFirst().get();
        var storage = this.getStorageProvider().createSchema(this.persistenceContext, schema);
        var entryType = schema.entityTypes().get("entry");
        var firstEntry = this.persistenceContext.newEntity("first", entryType);
        firstEntry.setProperty("title", "My first entry");
        firstEntry.setProperty("published", true);
        firstEntry.setProperty("created", Instant.now().truncatedTo(ChronoUnit.MILLIS));
        firstEntry.setProperty("category", "POLITICS");
        firstEntry.setProperty("readcount", 15);
        firstEntry.setProperty("tags", List.of("election", "politics"));
        firstEntry.setProperty("author", this.persistenceContext.newEmbedded(
                entryType.properties().stream()
                        .filter(etp -> etp.name().equals("author"))
                        .map(etp -> (EmbeddedPropertyType)etp.type())
                        .findFirst().orElseThrow(),
                entryType,
                List.of("author"),
                Map.of(
                        "name", "Max Mustermann",
                        "subtext", "The first of all authors.")));
        storage.save(firstEntry);
        var resultFromStorage = storage.find(entryType, "first").get();
        Assertions.assertEquals(firstEntry, resultFromStorage);
    }
    
    @Test
    public void testSaveAndLoadList() throws IOException, PersistenceException {
        var provider = this.persistenceContext.importSchemaFromSingleJson(AbstractStorageProviderTest.class.getResourceAsStream("AbstractStorageProviderTest_schema.json"));
        var schema = provider.getSchemas().stream().findFirst().get();
        var storage = this.getStorageProvider().createSchema(this.persistenceContext, schema);
        var entryType = schema.entityTypes().get("entry");
        var firstEntry = this.persistenceContext.newEntity("first", entryType);
        firstEntry.setProperty("title", "My first entry");
        firstEntry.setProperty("published", true);
        firstEntry.setProperty("created", Instant.now().truncatedTo(ChronoUnit.MILLIS));
        firstEntry.setProperty("category", "POLITICS");
        firstEntry.setProperty("readcount", 15);
        firstEntry.setProperty("tags", List.of("election", "politics"));
        //firstEntry.setProperty("author", Map.of("name", "Max Mustermann", "subtext", "The first of all authors."));
        storage.save(firstEntry);
        var secondEntry = this.persistenceContext.newEntity("second", entryType);
        secondEntry.setProperty("title", "My second entry");
        secondEntry.setProperty("published", true);
        secondEntry.setProperty("created", Instant.now().truncatedTo(ChronoUnit.MILLIS));
        secondEntry.setProperty("category", "POLITICS");
        secondEntry.setProperty("readcount", 12);
        secondEntry.setProperty("tags", List.of("election", "politics"));
        //secondEntry.setProperty("author", Map.of("name", "Max Mustermann", "subtext", "The first of all authors."));
        storage.save(secondEntry);
        var resultFromStorage = storage.findAll(entryType).collect(Collectors.toList());
        Assertions.assertEquals(firstEntry, resultFromStorage.get(0));
        Assertions.assertEquals(secondEntry, resultFromStorage.get(1));
    }
}
