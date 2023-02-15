package cloud.quinimbus.persistence.api.storage;

import cloud.quinimbus.persistence.api.schema.EntityTypeMigration;
import java.util.List;

public interface PersistenceSchemaStorageMigrator {
    
    void runEntityTypeMigration(String entityType, EntityTypeMigration migration, List<String> path);
}
