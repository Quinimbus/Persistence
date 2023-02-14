package cloud.quinimbus.persistence.api.storage;

import cloud.quinimbus.persistence.api.schema.EntityTypeMigration;

public interface PersistenceSchemaStorageMigrator {
    
    void runEntityTypeMigration(String entityType, EntityTypeMigration migration);
}
