package cloud.quinimbus.persistence.storage.mongo;

import static cloud.quinimbus.persistence.api.schema.EntityTypeProperty.Structure.LIST;
import static cloud.quinimbus.persistence.api.schema.EntityTypeProperty.Structure.SINGLE;
import static cloud.quinimbus.persistence.api.schema.migrations.PropertyValueMappingMigrationType.MissingMappingOperation.KEEP;
import static cloud.quinimbus.persistence.api.schema.migrations.PropertyValueMappingMigrationType.MissingMappingOperation.SET_TO_NULL;
import static cloud.quinimbus.persistence.storage.mongo.Documents.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.EntityTypeProperty;
import cloud.quinimbus.persistence.api.schema.StructuredObjectType;
import cloud.quinimbus.persistence.api.schema.migrations.PropertyAddMigrationType;
import cloud.quinimbus.persistence.api.schema.migrations.PropertyValueMappingMigrationType;
import cloud.quinimbus.persistence.api.schema.migrations.PropertyValueMappingMigrationType.Mapping;
import cloud.quinimbus.persistence.api.schema.properties.EmbeddedPropertyType;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorageMigrator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bson.BsonType;
import org.bson.conversions.Bson;

public class MongoSchemaStorageMigrator implements PersistenceSchemaStorageMigrator {

    private final MongoSchemaStorage storage;

    public MongoSchemaStorageMigrator(MongoSchemaStorage storage) {
        this.storage = storage;
    }

    @Override
    public void runPropertyAddMigration(EntityType entityType, PropertyAddMigrationType pamt, List<String> path)
            throws PersistenceException {
        var pipeline = path.isEmpty()
                ? setByMap(pamt.properties())
                : setByMap(pamt.properties().entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> "%s.%s".formatted(pathToPrefix(entityType, path), e.getKey()),
                                Map.Entry::getValue)));
        System.out.println("[runPropertyAddMigration] %s - %s - pipeline: %s"
                .formatted(
                        entityType.id(),
                        path.stream().collect(Collectors.joining(".")),
                        pipeline.toBsonDocument().toJson()));
        this.storage.getDatabase().getCollection(entityType.id()).updateMany(empty(), pipeline);
    }

    @Override
    public void runPropertyValueMappingMigrationType(
            EntityType entityType, PropertyValueMappingMigrationType pvmmt, List<String> path)
            throws PersistenceException {
        System.out.println("[runPropertyValueMappingMigrationType] %s - %s - %s"
                .formatted(
                        entityType.id(),
                        path.isEmpty() ? "<empty>" : path.stream().collect(Collectors.joining(".")),
                        pvmmt.field()));
        var property = getPropertyType(entityType, pvmmt.field(), path)
                .orElseThrow(() -> new IllegalStateException("field %s not found on type %s and path %s"
                        .formatted(pvmmt.field(), entityType.id(), path.stream().collect(Collectors.joining(".")))));
        var rules = buildMappingRules(pvmmt.mappings());
        var pathString = path.stream().collect(Collectors.joining("."));
        var field = path.isEmpty() ? "%s".formatted(pvmmt.field()) : "%s.%s".formatted(pathString, pvmmt.field());
        var fieldSelector =
                switch (property.structure()) {
                    case SINGLE -> "$%s".formatted(field);
                    case LIST -> "$$x";
                    default ->
                        throw new PersistenceException(
                                "Fields of structure type %s not supported for mapping migration in Mongo storage"
                                        .formatted(property.structure().name()));
                };
        var inputExpr = cond(isNumber(fieldSelector)).then(toStr(fieldSelector)).orElse(fieldSelector);
        var reducedValue = reduceFirstMatch(
                inputExpr,
                switch (pvmmt.missingMappingOperation()) {
                    case KEEP -> fieldSelector;
                    case SET_TO_NULL -> null;
                },
                rules);
        var pipeline = set(
                field,
                switch (property.structure()) {
                    case SINGLE -> reducedValue;
                    case LIST -> map("$%s".formatted(pvmmt.field()), "x", reducedValue);
                    default ->
                        throw new PersistenceException(
                                "Fields of structure type %s not supported for mapping migration in Mongo storage"
                                        .formatted(property.structure().name()));
                });
        var filter = or(type(field, BsonType.STRING), type(field, BsonType.ARRAY), type(field, "number"));
        System.out.println("[runPropertyValueMappingMigrationType] %s - %s - %s - pipeline: %s"
                .formatted(
                        entityType.id(),
                        path.isEmpty() ? "<empty>" : path.stream().collect(Collectors.joining(".")),
                        pvmmt.field(),
                        pipeline.toBsonDocument().toJson()));
        this.storage.getDatabase().getCollection(entityType.id()).updateMany(filter, List.of(pipeline));
    }

    private String pathToPrefix(StructuredObjectType entityType, List<String> path) {
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
                throw new IllegalArgumentException(
                        "An property path with more than 1 element is only allowed in embedded scenarios");
            }
        }
    }

    private static List<Bson> buildMappingRules(List<Mapping> mappings) {
        var rules = new ArrayList<Bson>(mappings.size());
        for (Mapping m : mappings) {
            String pattern;
            String options = "";
            switch (m.operator()) {
                case EQUALS -> {
                    pattern = "^" + Pattern.quote(m.oldValue()) + "$";
                }
                case EQUALS_IGNORE_CASE -> {
                    pattern = "^" + Pattern.quote(m.oldValue()) + "$";
                    options = "i";
                }
                case REGEX -> {
                    pattern = m.oldValue();
                }
                default -> throw new IllegalArgumentException("Unknown operator: " + m.operator());
            }
            rules.add(doc(entry("pattern", pattern), entry("options", options), entry("out", m.newValue())));
        }
        return rules;
    }

    private static Bson reduceFirstMatch(Bson inputExpr, String initialValue, List<Bson> rules) {
        return let(
                doc(
                        "acc",
                        reduce(
                                rules,
                                doc(entry("m", false), entry("v", initialValue)),
                                cond("$$value.m")
                                        .then("$$value")
                                        .orElse(cond(regexMatch(inputExpr, "$$this.pattern", "$$this.options"))
                                                .then(doc(entry("m", true), entry("v", "$$this.out")))
                                                .orElse("$$value")))),
                "$$acc.v");
    }

    private Bson setByMap(Map<String, Object> properties) {
        return combine(properties.entrySet().stream()
                .map(e -> set(e.getKey(), e.getValue()))
                .toList());
    }

    private Optional<EntityTypeProperty> getPropertyType(StructuredObjectType type, String field, List<String> path) {
        if (path.isEmpty()) {
            return type.property(field);
        } else {
            return type.embeddedPropertyType(path.get(0))
                    .flatMap(et -> getPropertyType(et, field, path.subList(1, path.size())));
        }
    }
}
