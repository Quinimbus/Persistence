package cloud.quinimbus.persistence.common.storage;

import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.entity.Entity;
import cloud.quinimbus.persistence.api.filter.PropertyFilter;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.Metadata;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorage;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorageMigrator;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import name.falgout.jeffrey.throwing.stream.ThrowingStream;

public abstract class PersistenceSchemaStorageDelegate implements PersistenceSchemaStorage {
    
    @Getter
    private final PersistenceSchemaStorage delegate;

    public PersistenceSchemaStorageDelegate(PersistenceSchemaStorage delegate) {
        this.delegate = delegate;
    }

    @Override
    public Metadata getSchemaMetadata() throws PersistenceException {
        return this.delegate.getSchemaMetadata();
    }

    @Override
    public void increaseSchemaVersion(Long version) throws PersistenceException {
        this.delegate.increaseSchemaVersion(version);
    }

    @Override
    public PersistenceSchemaStorageMigrator getMigrator() {
        return this.delegate.getMigrator();
    }

    @Override
    public void logMigrationRun(String identifier, String entityType, Long schemaVersion, Instant runAt) {
        this.delegate.logMigrationRun(identifier, entityType, schemaVersion, runAt);
    }

    @Override
    public <K> void save(Entity<K> entity) throws PersistenceException {
        this.delegate.save(entity);
    }

    @Override
    public <K> Optional<Entity<K>> find(EntityType type, K id) throws PersistenceException {
        return this.delegate.find(type, id);
    }

    @Override
    public <K> ThrowingStream<Entity<K>, PersistenceException> findFiltered(EntityType type, Set<? extends PropertyFilter> propertyFilters) {
        return this.delegate.findFiltered(type, propertyFilters);
    }

    @Override
    public <K> ThrowingStream<K, PersistenceException> findIDsFiltered(EntityType type, Set<? extends PropertyFilter> propertyFilters) {
        return this.delegate.findIDsFiltered(type, propertyFilters);
    }

    @Override
    public <K> void remove(EntityType type, K id) throws PersistenceException {
        this.delegate.remove(type, id);
    }

    @Override
    public <K> ThrowingStream<Entity<K>, PersistenceException> findAll(EntityType type) throws PersistenceException {
        return this.delegate.findAll(type);
    }

    @Override
    public <K> ThrowingStream<K, PersistenceException> findAllIDs(EntityType type) throws PersistenceException {
        return this.delegate.findAllIDs(type);
    }
    
    
}
