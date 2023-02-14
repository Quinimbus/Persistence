package cloud.quinimbus.persistence.schema.record;

import cloud.quinimbus.common.annotations.Provider;
import cloud.quinimbus.common.tools.Records;
import cloud.quinimbus.config.api.ConfigNode;
import cloud.quinimbus.persistence.api.annotation.Embeddable;
import cloud.quinimbus.persistence.api.annotation.Entity;
import cloud.quinimbus.persistence.api.annotation.EntityField;
import cloud.quinimbus.persistence.api.annotation.EntityIdField;
import cloud.quinimbus.persistence.api.annotation.FieldAddMigration;
import cloud.quinimbus.persistence.api.schema.InvalidSchemaException;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.EntityTypeMigration;
import cloud.quinimbus.persistence.api.schema.EntityTypeProperty;
import cloud.quinimbus.persistence.api.schema.EntityTypePropertyType;
import cloud.quinimbus.persistence.api.schema.PersistenceSchemaProvider;
import cloud.quinimbus.persistence.api.schema.Schema;
import cloud.quinimbus.persistence.api.schema.migrations.PropertyAddMigrationType;
import cloud.quinimbus.persistence.api.schema.properties.BooleanPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.EmbeddedPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.EnumPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.IntegerPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.LocalDatePropertyType;
import cloud.quinimbus.persistence.api.schema.properties.StringPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.TimestampPropertyType;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import name.falgout.jeffrey.throwing.stream.ThrowingStream;

@Provider(name = "Record classes schema provider", alias = "record", priority = 0)
public class RecordSchemaProvider implements PersistenceSchemaProvider {

    public Schema importSchema(Class<? extends Record>... recordClasses) throws InvalidSchemaException {
        return this.importSchema(Arrays.stream(recordClasses));
    }

    public Schema importSchema(Stream<Class<? extends Record>> recordClasses) throws InvalidSchemaException {
        return this.importSchema(ThrowingStream.of(recordClasses, InvalidSchemaException.class));
    }

    public Schema importSchema(ThrowingStream<Class<? extends Record>, InvalidSchemaException> recordClasses) throws InvalidSchemaException {
        var list = recordClasses.collect(Collectors.toList());
        if (list.isEmpty()) {
            throw new InvalidSchemaException("Record schema is empty");
        }
        var schemaDefs = ThrowingStream.of(list.stream(), InvalidSchemaException.class)
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
        return this.importSchema(schema.id(), schema.version(), list.stream());
    }

    public Schema importSchema(String id, Long version, Class<? extends Record>... recordClasses) throws InvalidSchemaException {
        return this.importSchema(id, version, Arrays.stream(recordClasses));
    }

    public Schema importSchema(String id, Long version, Stream<Class<? extends Record>> recordClasses) throws InvalidSchemaException {
        return this.importSchema(id, version, ThrowingStream.of(recordClasses, InvalidSchemaException.class));
    }

    public Schema importSchema(String id, Long version, ThrowingStream<Class<? extends Record>, InvalidSchemaException> recordClasses) throws InvalidSchemaException {
        var entityTypes = recordClasses
                .map(RecordSchemaProvider::typeOfRecord)
                .collect(Collectors.toMap(et -> et.id(), et -> et));
        return new Schema(id, entityTypes, version);
    }

    @Override
    public Schema loadSchema(Map<String, Object> params) throws InvalidSchemaException {
        if (params.containsKey("classes")) {
            var classes = params.get("classes");
            if (classes instanceof Iterable<?> classList) {
                ThrowingStream<Class<? extends Record>, InvalidSchemaException> recordClasses = ThrowingStream.of(StreamSupport.stream(classList.spliterator(), false), InvalidSchemaException.class)
                        .map(RecordSchemaProvider::mapToClass)
                        .map(RecordSchemaProvider::ensureRecord);
                return this.importSchema(recordClasses);
            } else {
                throw new InvalidSchemaException("Unknown type %s in configuration for classes".formatted(classes.getClass().getName()));
            }
        } else {
            throw new InvalidSchemaException("Cannot find classes key in the configuration");
        }
    }

    @Override
    public Schema loadSchema(ConfigNode node) throws InvalidSchemaException {
        ThrowingStream<Class<? extends Record>, InvalidSchemaException> recordClasses = ThrowingStream.of(node.asStringList("classes"), InvalidSchemaException.class)
                .map(RecordSchemaProvider::mapToClass)
                .map(RecordSchemaProvider::ensureRecord);
        return this.importSchema(recordClasses);
    }

    private static EntityType typeOfRecord(Class<? extends Record> recordClass) throws InvalidSchemaException {
        var id = Records.idFromRecordClass(recordClass);
        return new EntityType(
                id,
                propertiesOfRecord(recordClass),
                migrationsOfRecord(id, recordClass));
    }
    
    private static Set<EntityTypeProperty>propertiesOfRecord(Class<? extends Record> recordClass) throws InvalidSchemaException {
        return ThrowingStream.of(Arrays.stream(recordClass.getDeclaredFields()), InvalidSchemaException.class)
                        .filter(f -> f.getAnnotation(EntityIdField.class) == null)
                        .map(RecordSchemaProvider::propertyOfField)
                        .collect(Collectors.toSet());
    }

    private static EntityTypeProperty propertyOfField(Field field) throws InvalidSchemaException {
        if (Set.class.isAssignableFrom(field.getType()) || List.class.isAssignableFrom(field.getType()) || Map.class.isAssignableFrom(field.getType())) {
            var fieldAnno = field.getAnnotation(EntityField.class);
            if (fieldAnno == null) {
                throw new InvalidSchemaException("Missing @EntityField annotation for the field %s of type %s to define the type".formatted(field.getName(), field.getType().getName()));
            }
            return new cloud.quinimbus.persistence.api.schema.EntityTypeProperty(
                    field.getName(),
                    typeOfClass(fieldAnno.type()),
                    Set.class.isAssignableFrom(field.getType())
                            ? EntityTypeProperty.Structure.SET
                            : List.class.isAssignableFrom(field.getType())
                                    ? EntityTypeProperty.Structure.LIST
                                    :EntityTypeProperty.Structure.MAP);
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
        if (LocalDate.class.isAssignableFrom(cls)) {
            return new LocalDatePropertyType();
        }
        if (Instant.class.isAssignableFrom(cls)) {
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
    
    private static Class<?> mapToClass(Object e) throws InvalidSchemaException {
        if (e instanceof String s) {
            try {
                return Class.forName(s, true, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException ex) {
                throw new InvalidSchemaException("Cannot load class %s".formatted(s), ex);
            }
        } else if (e instanceof Class c) {
            return c;
        } else {
            if (e == null) {
                throw new InvalidSchemaException("null value in classes list found");
            }
            throw new InvalidSchemaException("Cannot read configured classes value <%s> of type %s as class".formatted(e
                    .toString(), e.getClass().getName()));
        }
    }
    
    private static Class<? extends Record> ensureRecord(Class<?> c) throws InvalidSchemaException {
        if (c.isRecord()) {
            return (Class<? extends Record>) c;
        } else {
            throw new InvalidSchemaException("The class %s is not a record class".formatted(c.getName()));
        }
    }

    private static Set<EntityTypeMigration> migrationsOfRecord(String entityId, Class<? extends Record> recordClass) throws InvalidSchemaException {
        var propertyBasedMigrations = ThrowingStream.of(Arrays.stream(recordClass.getDeclaredFields()), InvalidSchemaException.class)
                .map(f -> migrationOfField(entityId, f))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        return propertyBasedMigrations;
    }

    private static Optional<EntityTypeMigration> migrationOfField(String entityId, Field field) throws InvalidSchemaException {
        var fieldAdd = field.getAnnotation(FieldAddMigration.class);
        if (fieldAdd != null) {
            return Optional.of(new EntityTypeMigration<>("FieldAdd_%s_%s".formatted(entityId, field.getName()), fieldAdd.version(), new PropertyAddMigrationType(Map.of(field.getName(), fieldAdd.value()))));
        }
        return Optional.empty();
    }
}
