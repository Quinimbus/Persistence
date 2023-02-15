package cloud.quinimbus.persistence.storage.inmemory;

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
    public void runEntityTypeMigration(String entityType, EntityTypeMigration migration, List<String> path) {
        if (migration.type() instanceof PropertyAddMigrationType pamt) {
            this.entities.get(entityType).values()
                    .forEach(m -> pamt.properties()
                            .forEach((field, value) -> setProperty(m, field, path, value)));
        }
    }
    
    private void setProperty(Map<String, Object> entity, String field, List<String> path, Object value) {
        if (path.isEmpty()) {
            entity.put(field, value);
        } else {
            var nextPath = path.subList(1, path.size());
            setProperty((Map<String, Object>) entity.get(path.get(0)), field, nextPath, value);
        }
    }
}
