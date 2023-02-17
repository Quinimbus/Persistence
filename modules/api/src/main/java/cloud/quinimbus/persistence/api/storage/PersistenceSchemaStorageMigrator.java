package cloud.quinimbus.persistence.api.storage;

import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.EntityTypeMigration;
import java.util.List;

public interface PersistenceSchemaStorageMigrator {
    
    void runEntityTypeMigration(EntityType entityType, EntityTypeMigration migration, List<String> path) throws PersistenceException;
}
