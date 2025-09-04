package cloud.quinimbus.persistence.test.base;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.entity.EntityReaderInitialisationException;
import cloud.quinimbus.persistence.api.entity.EntityWriterInitialisationException;
import cloud.quinimbus.persistence.api.schema.InvalidSchemaException;
import cloud.quinimbus.persistence.api.storage.PersistenceStorageProvider;
import cloud.quinimbus.persistence.test.base.recordmigrationmodel.ModelV1;
import cloud.quinimbus.persistence.test.base.recordmigrationmodel.ModelV2;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.LogManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class AbstractRecordSchemaMigrationTest {

    private PersistenceContext persistenceContext;

    public abstract PersistenceStorageProvider getStorageProvider();

    public abstract Map<String, Object> getParams();

    private static record TestPair(ModelV1.BlogEntry before, ModelV2.BlogEntry after) {}

    private static final List<TestPair> TEST_DATA = Arrays.asList(
            new TestPair(
                    new ModelV1.BlogEntry(
                            "be1",
                            "My first entry",
                            new ModelV1.Author("John Doe", 1),
                            "news",
                            List.of("sports", "politic"),
                            1),
                    new ModelV2.BlogEntry(
                            "be1",
                            "My first entry",
                            new ModelV2.Author("John Doe", "unknown", ModelV2.AuthorRating.BESTSELLER),
                            ModelV2.Category.UNSORTED,
                            ModelV2.Type.NEWS,
                            List.of("sport", "politic"),
                            ModelV2.Importance.HIGH)),
            new TestPair(
                    new ModelV1.BlogEntry(
                            "be2",
                            "My second entry",
                            new ModelV1.Author("Jane Doe", 2),
                            "HOWTO",
                            List.of("tutorials"),
                            2),
                    new ModelV2.BlogEntry(
                            "be2",
                            "My second entry",
                            new ModelV2.Author("Jane Doe", "unknown", ModelV2.AuthorRating.MEDIUM),
                            ModelV2.Category.UNSORTED,
                            ModelV2.Type.HOWTO,
                            List.of("workshop"),
                            ModelV2.Importance.MEDIUM)),
            new TestPair(
                    new ModelV1.BlogEntry(
                            "be3", "My third entry", new ModelV1.Author("Peter Person", 3), "NEws", List.of(), 3),
                    new ModelV2.BlogEntry(
                            "be3",
                            "My third entry",
                            new ModelV2.Author("Peter Person", "unknown", ModelV2.AuthorRating.TRASH),
                            ModelV2.Category.UNSORTED,
                            null,
                            List.of(),
                            ModelV2.Importance.LOW)));

    @BeforeEach
    public void init() throws IOException {
        LogManager.getLogManager()
                .readConfiguration(AbstractRecordSchemaMigrationTest.class.getResourceAsStream("logging.properties"));
        this.persistenceContext =
                ServiceLoader.load(PersistenceContext.class).findFirst().get();
    }

    @Test
    public void testMigration()
            throws InvalidSchemaException, PersistenceException, EntityWriterInitialisationException,
                    EntityReaderInitialisationException {
        var schema = this.persistenceContext.importRecordSchema(ModelV1.BlogEntry.class);
        var storageProvider = getStorageProvider();
        var params = new LinkedHashMap<>(this.getParams());
        params.put("schema", schema.id());
        var storage = storageProvider.createSchema(persistenceContext, params);

        var blogEntryType = schema.entityTypes().get("blogEntry");
        var blogEntryReader = this.persistenceContext.getRecordEntityReader(blogEntryType, ModelV1.BlogEntry.class);
        for (TestPair pair : TEST_DATA) {
            storage.save(blogEntryReader.read(pair.before()));
        }

        schema = this.persistenceContext.importRecordSchema(ModelV2.BlogEntry.class);
        this.persistenceContext.upgradeSchema(storage);
        var metadata = storage.getSchemaMetadata();
        Assertions.assertEquals(Long.valueOf(2), metadata.version());
        Assertions.assertEquals(6, metadata.entityTypeMigrationRuns().size());

        blogEntryType = schema.entityTypes().get("blogEntry");
        var blogEntryWriter = this.persistenceContext.getRecordEntityWriter(blogEntryType, ModelV2.BlogEntry.class);

        for (TestPair pair : TEST_DATA) {
            var migratedEntry = storage.find(blogEntryType, pair.before().id())
                    .map(blogEntryWriter::write)
                    .orElseThrow();
            Assertions.assertEquals(pair.after(), migratedEntry);
        }
    }
}
