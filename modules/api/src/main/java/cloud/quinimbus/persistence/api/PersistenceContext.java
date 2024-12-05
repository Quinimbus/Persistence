package cloud.quinimbus.persistence.api;

import cloud.quinimbus.config.api.ConfigNode;
import cloud.quinimbus.persistence.api.entity.EmbeddedObject;
import cloud.quinimbus.persistence.api.entity.Entity;
import cloud.quinimbus.persistence.api.entity.EntityReader;
import cloud.quinimbus.persistence.api.entity.EntityReaderInitialisationException;
import cloud.quinimbus.persistence.api.entity.EntityWriter;
import cloud.quinimbus.persistence.api.entity.EntityWriterInitialisationException;
import cloud.quinimbus.persistence.api.entity.UnparseableValueException;
import cloud.quinimbus.persistence.api.lifecycle.LifecycleEvent;
import cloud.quinimbus.persistence.api.records.RecordEntityRegistry;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.InvalidSchemaException;
import cloud.quinimbus.persistence.api.schema.PersistenceSchemaProvider;
import cloud.quinimbus.persistence.api.schema.Schema;
import cloud.quinimbus.persistence.api.schema.properties.EmbeddedPropertyType;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorage;
import cloud.quinimbus.persistence.api.storage.PersistenceStorageProvider;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The persistence context is the central object for using Quinimbus Persistence.
 */
public interface PersistenceContext {

    /**
     * Get the storage provider registered by the given alias.
     *
     * @param <T> The type of the storage the provider will produce
     * @param alias The alias of the provider
     * @return An {@link Optional} containing the provider if present, an empty {@link Optional} otherwise
     */
    <T extends PersistenceSchemaStorage> Optional<? extends PersistenceStorageProvider<T>> getStorageProvider(
            String alias);

    /**
     * Get the schema provider registered by the given alias.
     *
     * @param alias The alias of the provider
     * @return An {@link Optional} containing the provider if present, an empty {@link Optional} otherwise
     */
    Optional<PersistenceSchemaProvider> getSchemaProvider(String alias);

    /**
     * Get the schame registered by the given id.
     *
     * @param id The id of the schema
     * @return An {@link Optional} containing the schema if present, an empty {@link Optional} otherwise
     */
    Optional<Schema> getSchema(String id);

    Optional<PersistenceSchemaStorage> getSchemaStorage(String id);

    PersistenceSchemaStorage setSchemaStorage(String id, PersistenceSchemaStorage storage);

    PersistenceSchemaStorage setInMemorySchemaStorage(String id);

    <K> Entity<K> newEntity(K id, EntityType type);

    <K> Entity<K> newEntity(K id, EntityType type, Map<String, Object> properties) throws UnparseableValueException;

    <K> Entity<K> newEntity(K id, EntityType type, Map<String, Object> properties, Map<String, Object> transientFields)
            throws UnparseableValueException;

    EmbeddedObject newEmbedded(
            EmbeddedPropertyType type, EntityType parentType, List<String> path, Map<String, Object> properties)
            throws UnparseableValueException;

    EmbeddedObject newEmbedded(
            EmbeddedPropertyType type,
            EntityType parentType,
            List<String> path,
            Map<String, Object> properties,
            Map<String, Object> transientFields)
            throws UnparseableValueException;

    Schema importSchema(PersistenceSchemaProvider provider) throws InvalidSchemaException;

    Schema importSchema(PersistenceSchemaProvider provider, ConfigNode configNode) throws InvalidSchemaException;

    Schema importSchemaFromSingleJson(Reader reader) throws InvalidSchemaException, IOException;

    Schema importRecordSchema(Class<? extends Record>... recordClasses) throws InvalidSchemaException;

    <T extends Record> EntityReader<T> getRecordEntityReader(EntityType type, Class<T> recordClass)
            throws EntityReaderInitialisationException;

    <T extends Record> EntityWriter<T> getRecordEntityWriter(EntityType type, Class<T> recordClass)
            throws EntityWriterInitialisationException;

    void upgradeSchema(PersistenceSchemaStorage storage) throws PersistenceException;

    RecordEntityRegistry getRecordEntityRegistry();

    <T extends LifecycleEvent<K>, K> void onLifecycleEvent(
            String schema, Class<T> eventType, EntityType type, Consumer<T> consumer);

    <T extends LifecycleEvent<K>, K> void onLifecycleEvent(
            String schema, Class<T> eventType, String typeId, Consumer<T> consumer);
}
