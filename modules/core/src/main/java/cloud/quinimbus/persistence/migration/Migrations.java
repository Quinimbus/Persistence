package cloud.quinimbus.persistence.migration;

import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.EntityTypeMigration;
import cloud.quinimbus.persistence.api.schema.EntityTypePropertyType;
import cloud.quinimbus.persistence.api.schema.Metadata;
import cloud.quinimbus.persistence.api.schema.Schema;
import cloud.quinimbus.persistence.api.schema.properties.EmbeddedPropertyType;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.java.Log;

@Log
public class Migrations {
    
    private static record EntityTypeMigrationEntry(EntityTypeMigration migration, List<String> path, String entityType) {
        
    }

    public static void upgradeSchema(PersistenceSchemaStorage storage, Function<String, Schema> schemaFinder) throws PersistenceException {
        var schemaMeta = storage.getSchemaMetadata();
        var schema = schemaFinder.apply(schemaMeta.id());
        log.fine(() -> "[upgradeSchema] Upgrading schema %s from version %d to version %d".formatted(schema.id(), schemaMeta.version(), schema.version()));
        var migrationsPerVersionAndType = schema.entityTypes().values().stream()
                .flatMap(Migrations::toMigrationEntries)
                .collect(
                        Collectors.groupingBy(
                                e -> e.migration().schemaVersion(),
                                Collectors.groupingBy(
                                        EntityTypeMigrationEntry::entityType,
                                        Collectors.toSet())));
        for (var version = 0L; version <= schema.version(); version++) {
            Migrations.checkMigrations(storage, schemaMeta, schema, version, migrationsPerVersionAndType.get(version));
        }
        storage.increaseSchemaVersion(schema.version());
    }
    
    private static Stream<EntityTypeMigrationEntry> toMigrationEntries(EntityType type) {
        var directMigrations = type.migrations().stream()
                .map(m -> new EntityTypeMigrationEntry(m, List.of(), type.id()));
        var embeddedMigrations = type.properties().stream()
                .flatMap(p -> toMigrationEntries(type.id(), p.name(), p.type(), List.of()));
        return Stream.concat(directMigrations, embeddedMigrations);
    }
    
    private static Stream<EntityTypeMigrationEntry> toMigrationEntries(String parentType, String name, EntityTypePropertyType type, List<String> path) {
        if (type instanceof EmbeddedPropertyType embedded) {
            var myPath = new ArrayList<>(path);
            myPath.add(name);
            var directMigrations = embedded.migrations().stream()
                .map(m -> new EntityTypeMigrationEntry(m, myPath, parentType));
            var embeddedMigrations = embedded.properties().stream()
                    .flatMap(p -> toMigrationEntries(parentType, p.name(), p.type(), myPath));
            return Stream.concat(directMigrations, embeddedMigrations);
        }
        return Stream.empty();
    }

    private static void checkMigrations(PersistenceSchemaStorage storage, Metadata schemaMetadata, Schema schema, long version, Map<String, Set<EntityTypeMigrationEntry>> migrations) throws PersistenceException {
        if (migrations == null || migrations.isEmpty()) {
            log.fine(() -> "[checkMigrations] no migrations needed for schema %s version %d".formatted(schema.id(), version));
            return;
        }
        log.fine(() -> "[checkMigrations] checking for migrations in schema %s for version %d".formatted(schema.id(), version));
        for (var migrationEntry : migrations.entrySet()) {
            checkMigrations(storage, schemaMetadata, schema, version, migrationEntry.getKey(), migrationEntry.getValue());
        }
    }
    
    private static void checkMigrations(PersistenceSchemaStorage storage, Metadata schemaMetadata, Schema schema, long version, String entityType, Set<EntityTypeMigrationEntry> migrations) throws PersistenceException {
        if (migrations == null || migrations.isEmpty()) {
            log.fine(() -> "[checkMigrations] no migrations needed for schema %s version %d entity type %s".formatted(schema.id(), version, entityType));
            return;
        }
        log.fine(() -> "[checkMigrations] checking for migrations in schema %s for version %d entity type %s".formatted(schema.id(), version, entityType));
        for (var m : migrations) {
            checkMigration(storage, schemaMetadata, schema, version, entityType, m);
        }
    }
    
    private static void checkMigration(PersistenceSchemaStorage storage, Metadata schemaMetadata, Schema schema, long version, String entityType, EntityTypeMigrationEntry migration) throws PersistenceException {
        log.fine(() -> "[checkMigration] checking for migration %s in schema %s for version %d entity type %s".formatted(migration.migration().name(), schema.id(), version, entityType));
        if (schemaMetadata.entityTypeMigrationRuns().stream().anyMatch(mr -> mr.entityType().equals(entityType) && mr.schemaVersion().equals(version) && mr.identifier().equals(migration.migration().name()))) {
            log.fine("[checkMigration] already run");
        } else {
            log.fine("[checkMigration] not yet run, running now");
            storage.getMigrator().runEntityTypeMigration(entityType, migration.migration(), migration.path());
            storage.logMigrationRun(migration.migration().name(), entityType, version, Instant.now());
        }
    }
}
