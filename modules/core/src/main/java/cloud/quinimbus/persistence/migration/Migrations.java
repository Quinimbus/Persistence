package cloud.quinimbus.persistence.migration;

import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.schema.EntityTypeMigration;
import cloud.quinimbus.persistence.api.schema.Metadata;
import cloud.quinimbus.persistence.api.schema.Schema;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorage;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import lombok.extern.java.Log;

@Log
public class Migrations {

    public static void upgradeSchema(PersistenceSchemaStorage storage, Function<String, Schema> schemaFinder) throws PersistenceException {
        var schemaMeta = storage.getSchemaMetadata();
        var schema = schemaFinder.apply(schemaMeta.id());
        log.fine(() -> "[upgradeSchema] Upgrading schema %s from version %d to version %d".formatted(schema.id(), schemaMeta.version(), schema.version()));
        var migrationsPerVersionAndType = schema.entityTypes().values().stream()
                .flatMap(et -> et.migrations().stream()
                .map(m -> Map.entry(et.id(), m)))
                .collect(
                        Collectors.groupingBy(
                                m -> m.getValue().schemaVersion(),
                                Collectors.groupingBy(
                                        Map.Entry::getKey,
                                        Collectors.mapping(
                                                Map.Entry::getValue,
                                                Collectors.toSet()))));
        LongStream.rangeClosed(0, schema.version()).forEach(version -> Migrations.checkMigrations(storage, schemaMeta, schema, version, migrationsPerVersionAndType.get(version)));
        storage.increaseSchemaVersion(schema.version());
    }

    private static void checkMigrations(PersistenceSchemaStorage storage, Metadata schemaMetadata, Schema schema, long version, Map<String, Set<EntityTypeMigration>> migrations) {
        if (migrations == null || migrations.isEmpty()) {
            log.fine(() -> "[checkMigrations] no migrations needed for schema %s version %d".formatted(schema.id(), version));
            return;
        }
        log.fine(() -> "[checkMigrations] checking for migrations in schema %s for version %d".formatted(schema.id(), version));
        migrations.entrySet().forEach(e -> checkMigrations(storage, schemaMetadata, schema, version, e.getKey(), e.getValue()));
    }
    
    private static void checkMigrations(PersistenceSchemaStorage storage, Metadata schemaMetadata, Schema schema, long version, String entityType, Set<EntityTypeMigration> migrations) {
        if (migrations == null || migrations.isEmpty()) {
            log.fine(() -> "[checkMigrations] no migrations needed for schema %s version %d entity type %s".formatted(schema.id(), version, entityType));
            return;
        }
        log.fine(() -> "[checkMigrations] checking for migrations in schema %s for version %d entity type %s".formatted(schema.id(), version, entityType));
        migrations.forEach(m -> checkMigration(storage, schemaMetadata, schema, version, entityType, m));
    }
    
    private static void checkMigration(PersistenceSchemaStorage storage, Metadata schemaMetadata, Schema schema, long version, String entityType, EntityTypeMigration migration) {
        log.fine(() -> "[checkMigration] checking for migration %s in schema %s for version %d entity type %s".formatted(migration.name(), schema.id(), version, entityType));
        if (schemaMetadata.entityTypeMigrationRuns().stream().anyMatch(mr -> mr.entityType().equals(entityType) && mr.schemaVersion().equals(version) && mr.identifier().equals(migration.name()))) {
            log.fine("[checkMigration] already run");
        } else {
            log.fine("[checkMigration] not yet run, running now");
            storage.getMigrator().runEntityTypeMigration(entityType, migration);
            storage.logMigrationRun(migration.name(), entityType, version, Instant.now());
        }
    }
}
