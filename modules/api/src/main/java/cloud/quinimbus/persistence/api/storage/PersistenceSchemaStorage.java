package cloud.quinimbus.persistence.api.storage;

import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.entity.Entity;
import cloud.quinimbus.persistence.api.filter.PropertyFilter;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.Metadata;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import name.falgout.jeffrey.throwing.stream.ThrowingStream;

public interface PersistenceSchemaStorage {
    
    Metadata getSchemaMetadata() throws PersistenceException;
    
    void increaseSchemaVersion(Long version)throws PersistenceException;
    
    PersistenceSchemaStorageMigrator getMigrator();
    
    void logMigrationRun(String identifier, String entityType, Long schemaVersion, Instant runAt);

    <K> void save(Entity<K> entity) throws PersistenceException;

    <K> Optional<Entity<K>> find(EntityType type, K id) throws PersistenceException;
    
    <K> ThrowingStream<Entity<K>, PersistenceException> findFiltered(EntityType type, Set<? extends PropertyFilter> propertyFilters);
    
    <K> ThrowingStream<K, PersistenceException> findIDsFiltered(EntityType type, Set<? extends PropertyFilter> propertyFilters);

    <K> void remove(EntityType type, K id) throws PersistenceException;

    <K> ThrowingStream<Entity<K>, PersistenceException> findAll(EntityType type) throws PersistenceException;

    <K> ThrowingStream<K, PersistenceException> findAllIDs(EntityType type) throws PersistenceException;
}
