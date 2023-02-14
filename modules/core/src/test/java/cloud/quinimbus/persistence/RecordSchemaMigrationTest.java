package cloud.quinimbus.persistence;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.entity.EntityReaderInitialisationException;
import cloud.quinimbus.persistence.api.entity.EntityWriterInitialisationException;
import cloud.quinimbus.persistence.api.schema.InvalidSchemaException;
import cloud.quinimbus.persistence.entity.reader.RecordEntityReader;
import cloud.quinimbus.persistence.entity.writer.RecordEntityWriter;
import cloud.quinimbus.persistence.recordmigrationmodel.ModelV1;
import cloud.quinimbus.persistence.recordmigrationmodel.ModelV2;
import cloud.quinimbus.persistence.storage.inmemory.InMemoryPersistenceStorageProvider;
import java.io.IOException;
import java.util.ServiceLoader;
import java.util.logging.LogManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RecordSchemaMigrationTest {

    private PersistenceContext persistenceContext;

    @BeforeEach
    public void init() throws IOException {
        LogManager.getLogManager().readConfiguration(RecordSchemaMigrationTest.class.getResourceAsStream("logging.properties"));
        this.persistenceContext = ServiceLoader.load(PersistenceContext.class).findFirst().get();
    }

    @Test
    public void testMigration() throws InvalidSchemaException, PersistenceException, EntityWriterInitialisationException, EntityReaderInitialisationException {
        var schema = this.persistenceContext.importRecordSchema(ModelV1.BlogEntry.class);
        var storageProvider = new InMemoryPersistenceStorageProvider();
        var storage = storageProvider.createSchema(persistenceContext, schema.id());
        
        var blogEntryType = schema.entityTypes().get("blogEntry");
        var blogEntryReader = new RecordEntityReader<>(blogEntryType, ModelV1.BlogEntry.class, "id");
        var entry = new ModelV1.BlogEntry("first", "My first entry");
        storage.save(blogEntryReader.read(entry));
        
        schema = this.persistenceContext.importRecordSchema(ModelV2.BlogEntry.class);
        this.persistenceContext.upgradeSchema(storage);
        var metadata = storage.getSchemaMetadata();
        Assertions.assertEquals(2, metadata.version());
        Assertions.assertEquals(1, metadata.entityTypeMigrationRuns().size());
        
        blogEntryType = schema.entityTypes().get("blogEntry");
        var blogEntryWriter = new RecordEntityWriter<>(blogEntryType, ModelV2.BlogEntry.class, "id");
        var migratedEntry = storage.find(blogEntryType, "first").map(blogEntryWriter::write).orElseThrow();
        Assertions.assertEquals(ModelV2.Category.UNSORTED, migratedEntry.category());
    }
}
