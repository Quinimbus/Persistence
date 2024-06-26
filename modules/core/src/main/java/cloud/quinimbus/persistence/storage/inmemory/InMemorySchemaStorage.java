package cloud.quinimbus.persistence.storage.inmemory;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.entity.Entity;
import cloud.quinimbus.persistence.api.filter.PropertyFilter;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.Metadata;
import cloud.quinimbus.persistence.api.schema.Metadata.MigrationRun;
import cloud.quinimbus.persistence.api.schema.Schema;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorage;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorageMigrator;
import cloud.quinimbus.tools.throwing.ThrowingOptional;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import name.falgout.jeffrey.throwing.stream.ThrowingStream;

public class InMemorySchemaStorage implements PersistenceSchemaStorage {

    private final PersistenceContext context;

    private final Map<String, Map<Object, Map<String, Object>>> entities;

    private final String schemaId;

    private Long schemaVersion;

    private final Instant creationTime;

    private final Set<MigrationRun> migrationRuns;

    public InMemorySchemaStorage(PersistenceContext context, Schema schema) {
        this.context = context;
        this.entities = schema.entityTypes().values().stream()
                .collect(Collectors.toMap(EntityType::id, et -> new LinkedHashMap<>()));
        this.schemaId = schema.id();
        this.schemaVersion = schema.version();
        this.creationTime = Instant.now();
        this.migrationRuns = new HashSet<>();
    }

    @Override
    public Metadata getSchemaMetadata() throws PersistenceException {
        return new Metadata(this.schemaId, this.schemaVersion, this.creationTime, Set.copyOf(this.migrationRuns));
    }

    @Override
    public void increaseSchemaVersion(Long version) throws PersistenceException {
        if (this.schemaVersion > version) {
            throw new PersistenceException(
                    "You cannot downgrade the schema version from %d to %d".formatted(this.schemaVersion, version));
        }
        this.schemaVersion = version;
    }

    @Override
    public PersistenceSchemaStorageMigrator getMigrator() {
        return new InMemorySchemaStorageMigrator(this.entities);
    }

    @Override
    public void logMigrationRun(String identifier, String entityType, Long schemaVersion, Instant runAt) {
        this.migrationRuns.add(new cloud.quinimbus.persistence.api.schema.Metadata.MigrationRun(
                identifier, entityType, schemaVersion, runAt));
    }

    @Override
    public <K> void save(Entity<K> entity) {
        this.entities.get(entity.getType().id()).put(entity.getId(), entity.asBasicMap());
    }

    @Override
    public <K> Optional<Entity<K>> find(EntityType type, K id) throws PersistenceException {
        return ThrowingOptional.ofNullable(this.entities.get(type.id()).get(id), PersistenceException.class)
                .map(m -> this.context.newEntity(id, type, m))
                .toOptional();
    }

    @Override
    public <K> ThrowingStream<Entity<K>, PersistenceException> findFiltered(
            EntityType type, Set<? extends PropertyFilter> propertyFilters) {
        return ThrowingStream.of(this.entities.get(type.id()).entrySet().stream(), PersistenceException.class)
                .filter(e -> propertyFilters.stream().allMatch(pf -> switch (pf.operator()) {
                    case EQUALS -> Objects.equals(e.getValue().get(pf.property()), pf.value());
                }))
                .map(e -> this.context.newEntity((K) e.getKey(), type, e.getValue()));
    }

    @Override
    public <K> ThrowingStream<K, PersistenceException> findIDsFiltered(
            EntityType type, Set<? extends PropertyFilter> propertyFilters) {
        return ThrowingStream.of(this.entities.get(type.id()).entrySet().stream(), PersistenceException.class)
                .filter(e -> propertyFilters.stream().allMatch(pf -> switch (pf.operator()) {
                    case EQUALS -> Objects.equals(e.getValue().get(pf.property()), pf.value());
                }))
                .map(e -> (K) e.getKey());
    }

    @Override
    public <K> ThrowingStream<Entity<K>, PersistenceException> findAll(EntityType type) {
        return ThrowingStream.of(this.entities.get(type.id()).entrySet().stream(), PersistenceException.class)
                .map(e -> this.context.newEntity((K) e.getKey(), type, e.getValue()));
    }

    @Override
    public <K> ThrowingStream<K, PersistenceException> findAllIDs(EntityType type) {
        return ThrowingStream.of(this.entities.get(type.id()).keySet().stream(), PersistenceException.class)
                .map(k -> (K) k);
    }

    @Override
    public <K> void remove(EntityType type, K id) throws PersistenceException {
        this.entities.get(type.id()).remove(id);
    }
}
