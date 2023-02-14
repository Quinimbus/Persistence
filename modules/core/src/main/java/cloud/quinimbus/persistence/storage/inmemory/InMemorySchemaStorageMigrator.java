package cloud.quinimbus.persistence.storage.inmemory;

import cloud.quinimbus.persistence.api.schema.EntityTypeMigration;
import cloud.quinimbus.persistence.api.schema.migrations.PropertyAddMigrationType;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorageMigrator;
import java.util.Map;

public class InMemorySchemaStorageMigrator implements PersistenceSchemaStorageMigrator {

    private final Map<String, Map<Object, Map<String, Object>>> entities;

    public InMemorySchemaStorageMigrator(Map<String, Map<Object, Map<String, Object>>> entities) {
        this.entities = entities;
    }

    @Override
    public void runEntityTypeMigration(String entityType, EntityTypeMigration migration) {
        if (migration.type() instanceof PropertyAddMigrationType pamt) {
            this.entities.get(entityType).values()
                    .forEach(m -> pamt.properties()
                            .forEach((field, value) -> m.put(field, value)));
        }
    }
}
