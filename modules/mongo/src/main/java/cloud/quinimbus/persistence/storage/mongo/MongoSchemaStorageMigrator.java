package cloud.quinimbus.persistence.storage.mongo;

import cloud.quinimbus.persistence.api.schema.EntityTypeMigration;
import cloud.quinimbus.persistence.api.schema.migrations.PropertyAddMigrationType;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorageMigrator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bson.Document;

public class MongoSchemaStorageMigrator implements PersistenceSchemaStorageMigrator {
    
    private final MongoSchemaStorage storage;

    public MongoSchemaStorageMigrator(MongoSchemaStorage storage) {
        this.storage = storage;
    }

    @Override
    public void runEntityTypeMigration(String entityType, EntityTypeMigration migration, List<String> path) {
        if (migration.type() instanceof PropertyAddMigrationType pamt) {
            if (path.isEmpty()) {
                this.storage.getDatabase().getCollection(entityType).updateMany(new Document(), new Document(Map.of("$set", pamt.properties())));
            } else {
                var prefix = path.stream().collect(Collectors.joining("."));
                var properties = pamt.properties().entrySet().stream()
                        .collect(
                                Collectors.toMap(
                                        e -> "%s.%s".formatted(prefix ,e.getKey()),
                                        Map.Entry::getValue));
                this.storage.getDatabase().getCollection(entityType).updateMany(new Document(), new Document(Map.of("$set", properties)));
            }
        }
    }
}
