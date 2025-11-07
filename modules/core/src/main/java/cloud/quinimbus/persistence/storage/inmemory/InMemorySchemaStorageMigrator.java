package cloud.quinimbus.persistence.storage.inmemory;

import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.EntityTypeProperty;
import cloud.quinimbus.persistence.api.schema.StructuredObjectType;
import cloud.quinimbus.persistence.api.schema.migrations.PropertyAddMigrationType;
import cloud.quinimbus.persistence.api.schema.migrations.PropertyValueMappingMigrationType;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorageMigrator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class InMemorySchemaStorageMigrator implements PersistenceSchemaStorageMigrator {

    private final Map<String, Map<Object, Map<String, Object>>> entities;

    public InMemorySchemaStorageMigrator(Map<String, Map<Object, Map<String, Object>>> entities) {
        this.entities = entities;
    }

    @Override
    public void runPropertyAddMigration(EntityType entityType, PropertyAddMigrationType pamt, List<String> path)
            throws PersistenceException {
        for (var m : this.entities.get(entityType.id()).values()) {
            for (var migrationEntry : pamt.properties().entrySet()) {
                setProperty(m, migrationEntry.getKey(), path, migrationEntry.getValue());
            }
        }
    }

    @Override
    public void runPropertyValueMappingMigrationType(
            EntityType entityType, PropertyValueMappingMigrationType pvmmt, List<String> path)
            throws PersistenceException {
        var property = getPropertyType(entityType, pvmmt.field(), path)
                .orElseThrow(() -> new IllegalStateException("field %s not found on type %s and path %s"
                        .formatted(pvmmt.field(), entityType.id(), path.stream().collect(Collectors.joining(".")))));
        for (var e : this.entities.get(entityType.id()).values()) {
            switch (property.structure()) {
                case SINGLE -> {
                    var oldValue = getPropertyAsString(e, pvmmt.field(), path);
                    if (oldValue != null) {
                        var newValue = mapValue(pvmmt, oldValue);
                        setProperty(e, pvmmt.field(), path, newValue);
                    }
                }
                case LIST -> {
                    var oldValues = getPropertyAsStringList(e, pvmmt.field(), path);
                    if (oldValues != null) {
                        var newValues =
                                oldValues.stream().map(v -> mapValue(pvmmt, v)).toList();
                        setProperty(e, pvmmt.field(), path, newValues);
                    }
                }
                case SET -> {
                    var oldValues = getPropertyAsStringSet(e, pvmmt.field(), path);
                    if (oldValues != null) {
                        var newValues = oldValues.stream()
                                .map(v -> mapValue(pvmmt, v))
                                .filter(v -> v != null)
                                .collect(Collectors.toSet());
                        setProperty(e, pvmmt.field(), path, newValues);
                    }
                }
                default ->
                    throw new PersistenceException(
                            "Fields of structure type %s not supported for mapping migration in InMemory storage"
                                    .formatted(property.structure().name()));
            }
        }
    }

    private String mapValue(PropertyValueMappingMigrationType pvmmt, String oldValue) {
        return pvmmt.mappings().stream()
                .filter(m -> switch (m.operator()) {
                    case EQUALS -> m.oldValue().equals(oldValue);
                    case EQUALS_IGNORE_CASE -> m.oldValue().equalsIgnoreCase(oldValue);
                    case REGEX -> oldValue.matches(m.oldValue());
                })
                .findFirst()
                .map(m -> m.newValue())
                .orElse(
                        switch (pvmmt.missingMappingOperation()) {
                            case KEEP -> oldValue;
                            case SET_TO_NULL -> null;
                        });
    }

    private void setProperty(Map<String, Object> entity, String field, List<String> path, Object value)
            throws PersistenceException {
        if (path.isEmpty()) {
            entity.put(field, value);
        } else {
            var nextPath = path.subList(1, path.size());
            var embeddedEntity = entity.get(path.get(0));
            if (embeddedEntity != null) {
                if (embeddedEntity instanceof Map embeddedEntityMap) {
                    setProperty((Map<String, Object>) embeddedEntityMap, field, nextPath, value);
                } else if (embeddedEntity instanceof List embeddedEntityList) {
                    for (var map : embeddedEntityList) {
                        setProperty((Map<String, Object>) map, field, nextPath, value);
                    }
                } else if (embeddedEntity instanceof Set embeddedEntitySet) {
                    for (var map : embeddedEntitySet) {
                        setProperty((Map<String, Object>) map, field, nextPath, value);
                    }
                } else {
                    throw new PersistenceException("expected a map but got %s"
                            .formatted(embeddedEntity.getClass().getName()));
                }
            }
        }
    }

    private Object getProperty(Map<String, Object> entity, String field, List<String> path) {
        if (path.isEmpty()) {
            return entity.get(field);
        } else {
            var nextPath = path.subList(1, path.size());
            var embeddedEntity = entity.get(path.get(0));
            if (embeddedEntity != null) {
                if (embeddedEntity instanceof Map embeddedEntityMap) {
                    return getProperty(embeddedEntityMap, field, nextPath);
                }
            }
            return null;
        }
    }

    private String getPropertyAsString(Map<String, Object> entity, String field, List<String> path) {
        var value = getProperty(entity, field, path);
        return switch (value) {
            case null -> null;
            case String s -> s;
            case Number n -> n.toString();
            default ->
                throw new IllegalArgumentException(
                        "Cannot read %s as string".formatted(value.getClass().getName()));
        };
    }

    private List<String> getPropertyAsStringList(Map<String, Object> entity, String field, List<String> path) {
        var value = getProperty(entity, field, path);
        return switch (value) {
            case null -> null;
            case List<?> list ->
                list.stream()
                        .map(e -> switch (e) {
                            case null -> null;
                            case String s -> s;
                            case Number n -> n.toString();
                            default ->
                                throw new IllegalArgumentException("Cannot read %s as string"
                                        .formatted(e.getClass().getName()));
                        })
                        .toList();
            default ->
                throw new IllegalArgumentException("Cannot read %s as string list"
                        .formatted(value.getClass().getName()));
        };
    }

    private Set<String> getPropertyAsStringSet(Map<String, Object> entity, String field, List<String> path) {
        var value = getProperty(entity, field, path);
        return switch (value) {
            case null -> null;
            case Set<?> set ->
                set.stream()
                        .map(e -> switch (e) {
                            case String s -> s;
                            case Number n -> n.toString();
                            default ->
                                throw new IllegalArgumentException("Cannot read %s as string"
                                        .formatted(e.getClass().getName()));
                        })
                        .collect(Collectors.toSet());
            default ->
                throw new IllegalArgumentException("Cannot read %s as string set"
                        .formatted(value.getClass().getName()));
        };
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
