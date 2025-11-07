package cloud.quinimbus.persistence.api.storage;

import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.EntityTypeMigration;
import cloud.quinimbus.persistence.api.schema.migrations.PropertyAddMigrationType;
import cloud.quinimbus.persistence.api.schema.migrations.PropertyValueMappingMigrationType;
import java.util.List;

public interface PersistenceSchemaStorageMigrator {

    default void runEntityTypeMigration(EntityType entityType, EntityTypeMigration migration, List<String> path)
            throws PersistenceException {
        switch (migration.type()) {
            case PropertyAddMigrationType pamt -> runPropertyAddMigration(entityType, pamt, path);
            case PropertyValueMappingMigrationType pvmmt ->
                runPropertyValueMappingMigrationType(entityType, pvmmt, path);
        }
    }

    void runPropertyAddMigration(EntityType entityType, PropertyAddMigrationType pamt, List<String> path)
            throws PersistenceException;

    void runPropertyValueMappingMigrationType(
            EntityType entityType, PropertyValueMappingMigrationType pvmmt, List<String> path)
            throws PersistenceException;
}
