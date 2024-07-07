package cloud.quinimbus.persistence.entity.reader;

import cloud.quinimbus.persistence.api.annotation.EntityField;
import cloud.quinimbus.persistence.api.entity.EntityReaderInitialisationException;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.EntityTypeProperty;
import cloud.quinimbus.persistence.api.schema.properties.EmbeddedPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.EnumPropertyType;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import name.falgout.jeffrey.throwing.stream.ThrowingStream;

public class AbstractRecordReader<T extends Record> {

    private final Map<String, Method> propertyFieldGetters;

    private final Map<String, Function<Object, Object>> propertyFieldValueReaders;
    
    private final Map<String, Method> transientFieldGetters;

    public AbstractRecordReader(EntityType type, Set<EntityTypeProperty> properties, Class<T> recordClass)
            throws EntityReaderInitialisationException {
        try {
            this.propertyFieldGetters = createPropertyFieldGetters(properties, recordClass);
            this.transientFieldGetters = createTransientFieldGetters(
                    properties.stream().map(EntityTypeProperty::name).collect(Collectors.toSet()), recordClass);
            this.propertyFieldValueReaders = createPropertyFieldValueReaders(properties, type, recordClass);
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new EntityReaderInitialisationException(
                    "Cannot initialize the reader for the record class %s".formatted(recordClass.getName()), ex);
        }
    }

    private static <T extends Record> Map<String, Method> createPropertyFieldGetters(
            Set<EntityTypeProperty> properties, Class<T> recordClass) throws NoSuchMethodException {
        return ThrowingStream.of(properties.stream(), NoSuchMethodException.class)
                .map(p -> Map.entry(p.name(), recordClass.getMethod(p.name())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static <T extends Record> Map<String, Method> createTransientFieldGetters(
            Set<String> properties, Class<T> recordClass) {
        return Arrays.stream(recordClass.getRecordComponents())
                .filter(rc -> !properties.contains(rc.getName()))
                .collect(Collectors.toMap(RecordComponent::getName, RecordComponent::getAccessor));
    }

    private static <T extends Record> Map<String, Function<Object, Object>> createPropertyFieldValueReaders(
            Set<EntityTypeProperty> properties, EntityType type, Class<T> recordClass)
            throws EntityReaderInitialisationException {
        return ThrowingStream.of(properties.stream(), EntityReaderInitialisationException.class)
                .map(p -> Map.entry(p.name(), createPropertyFieldValueReader(type, p, recordClass)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static <T extends Record> Function<Object, Object> createPropertyFieldValueReader(
            EntityType type, EntityTypeProperty t, Class<T> recordClass) throws EntityReaderInitialisationException {
        if (t.type() instanceof EmbeddedPropertyType ept) {
            try {
                var targetClass =
                        switch (t.structure()) {
                            case SINGLE -> (Class<Record>)
                                    recordClass.getMethod(t.name()).getReturnType();
                            case LIST, SET, MAP -> (Class<Record>) recordClass
                                    .getDeclaredField(t.name())
                                    .getAnnotation(EntityField.class)
                                    .type();
                        };
                var reader = new EmbeddedRecordReader(ept, targetClass, List.of(t.name()), type);
                return switch (t.structure()) {
                    case SINGLE -> reader::tryRead;
                    case LIST -> listReader(reader::tryRead);
                    case MAP -> mapReader(reader::tryRead);
                    case SET -> setReader(reader::tryRead);
                };
            } catch (ReflectiveOperationException | SecurityException ex) {
                throw new EntityReaderInitialisationException(
                        "Unable to find the getter method for %s".formatted(t.name()), ex);
            }
        } else if (t.type() instanceof EnumPropertyType) {
            Function<Object, Object> reader = v -> {
                if (v instanceof Enum e) {
                    return e.name();
                }
                throw new IllegalArgumentException();
            };
            return switch (t.structure()) {
                case SINGLE -> reader;
                case LIST -> listReader(reader);
                case MAP -> mapReader(reader);
                case SET -> setReader(reader);
            };
        } else {
            return Function.identity();
        }
    }

    private static Function<Object, Object> listReader(Function<Object, Object> singleReader) {
        return vl -> {
            if (vl instanceof Collection c) {
                return c.stream().map(singleReader).toList();
            } else {
                throw new IllegalStateException();
            }
        };
    }

    private static Function<Object, Object> setReader(Function<Object, Object> singleReader) {
        return vl -> {
            if (vl instanceof Collection c) {
                return c.stream().map(singleReader).collect(Collectors.toSet());
            } else {
                throw new IllegalStateException();
            }
        };
    }

    private static Function<Object, Object> mapReader(Function<Object, Object> singleReader) {
        return vl -> {
            if (vl instanceof Map<?, ?> m) {
                return m.entrySet().stream()
                        .collect(Collectors.toMap(e -> e.getKey(), e -> singleReader.apply(e.getValue())));
            } else {
                throw new IllegalStateException();
            }
        };
    }

    protected Map<String, Object> getProperties(T source) throws ReflectiveOperationException {
        return ThrowingStream.of(this.propertyFieldGetters.entrySet().stream(), ReflectiveOperationException.class)
                .map(e -> Map.entry(e.getKey(), Optional.ofNullable(e.getValue().invoke(source))))
                .filter(e -> e.getValue().isPresent())
                .map(e -> Map.entry(
                        e.getKey(),
                        this.propertyFieldValueReaders
                                .get(e.getKey())
                                .apply(e.getValue().get())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    protected Map<String, Object> getTransientFields(T source) throws ReflectiveOperationException {
        return ThrowingStream.of(this.transientFieldGetters.entrySet().stream(), ReflectiveOperationException.class)
                .map(e -> Optional.ofNullable(e.getValue().invoke(source)).map(v -> Map.entry(e.getKey(), v)))
                .filter(Optional::isPresent)
                .map(Optional::orElseThrow)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
