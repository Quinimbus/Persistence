package cloud.quinimbus.persistence.storage.inmemory;

import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.schema.EntityTypeMigration;
import cloud.quinimbus.persistence.api.schema.migrations.PropertyAddMigrationType;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorageMigrator;
import java.util.List;
import java.util.Map;

public class InMemorySchemaStorageMigrator implements PersistenceSchemaStorageMigrator {

    private final Map<String, Map<Object, Map<String, Object>>> entities;

    public InMemorySchemaStorageMigrator(Map<String, Map<Object, Map<String, Object>>> entities) {
        this.entities = entities;
    }

    @Override
    public void runEntityTypeMigration(String entityType, EntityTypeMigration migration, List<String> path) throws PersistenceException {
        if (migration.type() instanceof PropertyAddMigrationType pamt) {
            for (var m : this.entities.get(entityType).values()) {
                for (var migrationEntry : pamt.properties().entrySet()) {
                    setProperty(m, migrationEntry.getKey(), path, migrationEntry.getValue());
                }
            }
        }
    }

    private void setProperty(Map<String, Object> entity, String field, List<String> path, Object value) throws PersistenceException {
        if (path.isEmpty()) {
            entity.put(field, value);
        } else {
            var nextPath = path.subList(1, path.size());
            var embeddedEntity = entity.get(path.get(0));
            if (embeddedEntity instanceof Map embeddedEntityMap) {
                setProperty((Map<String, Object>) embeddedEntityMap, field, nextPath, value);
            } else {
                throw new PersistenceException("expected a map but got %s".formatted(embeddedEntity.getClass().getName()));
            }
        }
    }
}
