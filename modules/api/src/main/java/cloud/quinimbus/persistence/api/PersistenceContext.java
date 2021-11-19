package cloud.quinimbus.persistence.api;

import cloud.quinimbus.persistence.api.entity.EmbeddedObject;
import cloud.quinimbus.persistence.api.schema.InvalidSchemaException;
import cloud.quinimbus.persistence.api.entity.Entity;
import cloud.quinimbus.persistence.api.entity.EntityReader;
import cloud.quinimbus.persistence.api.entity.EntityReaderInitialisationException;
import cloud.quinimbus.persistence.api.entity.EntityWriter;
import cloud.quinimbus.persistence.api.entity.EntityWriterInitialisationException;
import cloud.quinimbus.persistence.api.entity.UnparseableValueException;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.PersistenceSchemaProvider;
import cloud.quinimbus.persistence.api.schema.Schema;
import cloud.quinimbus.persistence.api.schema.properties.EmbeddedPropertyType;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PersistenceContext {

    Optional<Schema> getSchema(String id);

    Optional<PersistenceSchemaStorage> getSchemaStorage(String id);

    void setSchemaStorage(String id, PersistenceSchemaStorage storage);

    void setInMemorySchemaStorage(String id);

    <K> Entity<K> newEntity(K id, EntityType type);

    <K> Entity<K> newEntity(K id, EntityType type, Map<String, Object> properties) throws UnparseableValueException;

    EmbeddedObject newEmbedded(EmbeddedPropertyType type, EntityType parentType, List<String> path, Map<String, Object> properties) throws UnparseableValueException;

    PersistenceSchemaProvider importSchemaFromSingleJson(InputStream inputStream) throws IOException;

    PersistenceSchemaProvider importRecordSchema(Class<? extends Record>... recordClasses) throws InvalidSchemaException;

    <T extends Record> EntityReader<T> getRecordEntityReader(EntityType type, Class<T> recordClass) throws EntityReaderInitialisationException;

    <T extends Record> EntityWriter<T> getRecordEntityWriter(EntityType type, Class<T> recordClass) throws EntityWriterInitialisationException;
}
