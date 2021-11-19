package cloud.quinimbus.persistence.schema.record;

import cloud.quinimbus.persistence.api.annotation.Embeddable;
import cloud.quinimbus.persistence.api.annotation.Entity;
import cloud.quinimbus.persistence.api.annotation.EntityField;
import cloud.quinimbus.persistence.api.annotation.EntityIdField;
import cloud.quinimbus.persistence.api.schema.InvalidSchemaException;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.EntityTypeProperty;
import cloud.quinimbus.persistence.api.schema.EntityTypePropertyType;
import cloud.quinimbus.persistence.api.schema.PersistenceSchemaProvider;
import cloud.quinimbus.persistence.api.schema.Schema;
import cloud.quinimbus.persistence.api.schema.properties.BooleanPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.EmbeddedPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.EnumPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.IntegerPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.StringPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.TimestampPropertyType;
import cloud.quinimbus.persistence.common.Records;
import java.lang.reflect.Field;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import name.falgout.jeffrey.throwing.stream.ThrowingStream;

public class RecordSchemaProvider implements PersistenceSchemaProvider {

    private Map<String, Schema> schemas = new LinkedHashMap<>();

    public void importSchema(Class<? extends Record>... recordClasses) throws InvalidSchemaException {
        var schemaDefs = ThrowingStream.of(Arrays.stream(recordClasses), InvalidSchemaException.class)
                .map(rc -> Optional.ofNullable(rc.getAnnotation(Entity.class))
                        .orElseThrow(() ->
                                new InvalidSchemaException("Type %s is missing the @Entity annotation and no schema information are given"
                                        .formatted(rc.getName()))))
                .map(Entity::schema)
                .distinct()
                .collect(Collectors.toList());
        if (schemaDefs.size() > 1) {
            throw new InvalidSchemaException("Different schema definitions found on the record types for the same schema");
        }
        var schema = schemaDefs.get(0);
        this.importSchema(schema.id(), schema.version(), recordClasses);
    }

    public void importSchema(String id, Long version, Class<? extends Record>... recordClasses) throws InvalidSchemaException {
        var entityTypes = ThrowingStream.of(Arrays.stream(recordClasses), InvalidSchemaException.class)
                .map(RecordSchemaProvider::typeOfRecord)
                .collect(Collectors.toMap(et -> et.id(), et -> et));
        var schema = new Schema(id, entityTypes, version);
        this.schemas.put(id, schema);
    }

    @Override
    public Set<Schema> getSchemas() {
        return Set.copyOf(schemas.values());
    }

    private static EntityType typeOfRecord(Class<? extends Record> recordClass) throws InvalidSchemaException {
        return new EntityType(
                Records.idFromClassName(recordClass.getSimpleName()),
                ThrowingStream.of(Arrays.stream(recordClass.getDeclaredFields()), InvalidSchemaException.class)
                        .filter(f -> f.getAnnotation(EntityIdField.class) == null)
                        .map(RecordSchemaProvider::propertyOfField)
                        .collect(Collectors.toSet()));
    }

    private static EntityTypeProperty propertyOfField(Field field) throws InvalidSchemaException {
        if (Set.class.isAssignableFrom(field.getType()) || List.class.isAssignableFrom(field.getType())) {
            var fieldAnno = field.getAnnotation(EntityField.class);
            if (fieldAnno == null) {
                throw new InvalidSchemaException("Missing @EntityField annotation for the field %s of type %s to define the type".formatted(field.getName(), field.getType().getName()));
            }
            return new cloud.quinimbus.persistence.api.schema.EntityTypeProperty(
                    field.getName(),
                    typeOfClass(fieldAnno.type()),
                    Set.class.isAssignableFrom(field.getType()) ? EntityTypeProperty.Structure.SET : EntityTypeProperty.Structure.LIST);
        } else {
            return new cloud.quinimbus.persistence.api.schema.EntityTypeProperty(
                    field.getName(),
                    typeOfClass(field.getType()),
                    EntityTypeProperty.Structure.SINGLE);
        }
    }

    private static EntityTypePropertyType typeOfClass(Class cls) throws InvalidSchemaException {
        if (String.class.equals(cls)) {
            return new StringPropertyType();
        }
        if (Boolean.class.equals(cls) || boolean.class.equals(cls)) {
            return new BooleanPropertyType();
        }
        if (Temporal.class.isAssignableFrom(cls)) {
            return new TimestampPropertyType();
        }
        if (Enum.class.isAssignableFrom(cls)) {
            var values = Arrays.stream(cls.getEnumConstants()).map(Object::toString).toList();
            return new EnumPropertyType(values);
        }
        if (Long.class.isAssignableFrom(cls) || long.class.isAssignableFrom(cls)
            || Integer.class.isAssignableFrom(cls) || int.class.isAssignableFrom(cls)
            || Short.class.isAssignableFrom(cls) || short.class.isAssignableFrom(cls)
            || Byte.class.isAssignableFrom(cls) || byte.class.isAssignableFrom(cls)) {
            return new IntegerPropertyType();
        }
        if (Record.class.isAssignableFrom(cls)) {
            if (cls.getAnnotation(Embeddable.class) != null) {
                return new EmbeddedPropertyType(ThrowingStream.of(Arrays.stream(cls.getDeclaredFields()), InvalidSchemaException.class)
                        .map(RecordSchemaProvider::propertyOfField)
                        .collect(Collectors.toSet()));
            }
        }
        throw new InvalidSchemaException("Cannot map class %s to an entity property type".formatted(cls.getName()));
    }
}
