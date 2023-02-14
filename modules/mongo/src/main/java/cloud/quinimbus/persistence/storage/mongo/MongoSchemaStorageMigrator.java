package cloud.quinimbus.persistence.storage.mongo;

import cloud.quinimbus.persistence.api.schema.EntityTypeMigration;
import cloud.quinimbus.persistence.api.schema.migrations.PropertyAddMigrationType;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorageMigrator;
import java.util.Map;
import org.bson.Document;

public class MongoSchemaStorageMigrator implements PersistenceSchemaStorageMigrator {
    
    private final MongoSchemaStorage storage;

    public MongoSchemaStorageMigrator(MongoSchemaStorage storage) {
        this.storage = storage;
    }

    @Override
    public void runEntityTypeMigration(String entityType, EntityTypeMigration migration) {
        if (migration.type() instanceof PropertyAddMigrationType pamt) {
            this.storage.getDatabase().getCollection(entityType).updateMany(new Document(), new Document(Map.of("$set", pamt.properties())));
        }
    }
}
