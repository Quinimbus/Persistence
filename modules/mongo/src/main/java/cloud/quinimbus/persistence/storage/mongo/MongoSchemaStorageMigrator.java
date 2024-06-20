package cloud.quinimbus.persistence.storage.mongo;

import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.EntityTypeMigration;
import cloud.quinimbus.persistence.api.schema.StructuredObjectType;
import cloud.quinimbus.persistence.api.schema.migrations.PropertyAddMigrationType;
import cloud.quinimbus.persistence.api.schema.properties.EmbeddedPropertyType;
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
    public void runEntityTypeMigration(EntityType entityType, EntityTypeMigration migration, List<String> path)
            throws PersistenceException {
        if (migration.type() instanceof PropertyAddMigrationType pamt) {
            if (path.isEmpty()) {
                this.storage
                        .getDatabase()
                        .getCollection(entityType.id())
                        .updateMany(new Document(), new Document(Map.of("$set", pamt.properties())));
            } else {
                var prefix = pathToPrefix(entityType, path);
                var properties = pamt.properties().entrySet().stream()
                        .collect(Collectors.toMap(e -> "%s.%s".formatted(prefix, e.getKey()), Map.Entry::getValue));
                this.storage
                        .getDatabase()
                        .getCollection(entityType.id())
                        .updateMany(new Document(), new Document(Map.of("$set", properties)));
            }
        }
    }

    private String pathToPrefix(StructuredObjectType entityType, List<String> path) throws PersistenceException {
        var currentPath = path.get(0);
        var currentProperty = entityType.property(currentPath).orElseThrow();
        if (currentProperty.type() instanceof EmbeddedPropertyType) {
            switch (currentProperty.structure()) {
                case LIST, SET -> currentPath = currentPath.concat(".$[]");
            }
        }
        if (path.size() == 1) {
            return currentPath;
        } else {
            if (currentProperty.type() instanceof EmbeddedPropertyType ept) {
                return "%s.%s".formatted(currentPath, pathToPrefix(ept, path.subList(1, path.size())));
            } else {
                throw new PersistenceException(
                        "An property path with more than 1 element is only allowed in embedded scenarios");
            }
        }
    }
}
