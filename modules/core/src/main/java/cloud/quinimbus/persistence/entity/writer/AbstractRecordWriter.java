package cloud.quinimbus.persistence.entity.writer;

import cloud.quinimbus.persistence.api.annotation.EntityField;
import cloud.quinimbus.persistence.api.entity.Entity;
import cloud.quinimbus.persistence.api.entity.EntityWriterInitialisationException;
import cloud.quinimbus.persistence.api.entity.StructuredObject;
import cloud.quinimbus.persistence.api.schema.EntityTypeProperty;
import cloud.quinimbus.persistence.api.schema.properties.EmbeddedPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.EnumPropertyType;
import cloud.quinimbus.persistence.exception.EntityWriterWriteException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import name.falgout.jeffrey.throwing.stream.ThrowingStream;

public class AbstractRecordWriter<T extends Record> {
    
    private final Class<T> recordClass;

    private final Constructor<T> constructor;

    private final List<Function<StructuredObject, Object>> constructorParameterGetters;

    private final List<Function<Object, Object>> constructorParameterValueWriters;

    public AbstractRecordWriter(Class<T> recordClass, Set<EntityTypeProperty> properties, String idField) throws EntityWriterInitialisationException {
        this.recordClass = recordClass;
        this.constructor = findConstructor(recordClass);
        this.constructorParameterGetters = createConstructorParameterGetters(this.constructor, idField);
        this.constructorParameterValueWriters = createConstructorParameterValueWriters(this.constructor, properties, recordClass);
    }

    private static <T extends Record> Constructor<T> findConstructor(Class<T> recordClass) {
        return Arrays.stream((Constructor<T>[]) recordClass.getConstructors())
                .sorted((c1, c2) -> c1.getParameterCount() - c2.getParameterCount())
                .findFirst().get();
    }

    private static List<Function<StructuredObject, Object>> createConstructorParameterGetters(Constructor<?> constructor, String idField) {
        return Arrays.stream(constructor.getParameters())
                .map(Parameter::getName)
                .map(n -> n.equals(idField)
                        ? (Function<StructuredObject, Object>) o -> o instanceof Entity e ? e.getId() : null
                        : (Function<StructuredObject, Object>) o -> o.getProperty(n))
                .toList();
    }

    private static <T extends Record> List<Function<Object, Object>> createConstructorParameterValueWriters(Constructor<?> constructor, Set<EntityTypeProperty> properties, Class<T> recordClass) throws EntityWriterInitialisationException {
        var propertyMap = properties.stream().collect(Collectors.toMap(EntityTypeProperty::name, Function.identity()));
        return ThrowingStream.of(Arrays.stream(constructor.getParameters()), EntityWriterInitialisationException.class)
                .map(Parameter::getName)
                .map(propertyMap::get)
                .map(p -> createConstructorParameterValueWriter(p, recordClass))
                .collect(Collectors.toList());
    }
    
    private static <T extends Record> Function<Object, Object> createConstructorParameterValueWriter(EntityTypeProperty t, Class<T> recordClass) throws EntityWriterInitialisationException {
        if (t != null && t.type() instanceof EmbeddedPropertyType ept) {
            try {
                var targetClass = switch (t.structure()) {
                    case SINGLE -> (Class<Record>) recordClass.getMethod(t.name()).getReturnType();
                    case LIST, SET, MAP -> (Class<Record>) recordClass.getDeclaredField(t.name()).getAnnotation(EntityField.class).type();
                };
                var writer = new EmbeddedRecordWriter<Record>(targetClass, ept);
                return switch (t.structure()) {
                    case SINGLE -> writer::tryWrite;
                    case LIST -> listWriter(writer::tryWrite);
                    case MAP -> mapWriter(writer::tryWrite);
                    case SET -> setWriter(writer::tryWrite);
                };
            } catch (ReflectiveOperationException | SecurityException ex) {
                throw new EntityWriterInitialisationException("Unable to find the getter method ot field for %s".formatted(t.name()), ex);
            }
        } else if (t != null && t.type() instanceof EnumPropertyType) {
            try {
                var targetClass = switch (t.structure()) {
                    case SINGLE -> (Class<Record>) recordClass.getMethod(t.name()).getReturnType();
                    case LIST, SET, MAP -> (Class<Record>) recordClass.getDeclaredField(t.name()).getAnnotation(EntityField.class).type();
                };
                var valueOfMethod = targetClass.getMethod("valueOf", String.class);
                Function<Object, Object> writer = v -> {
                    if (v == null) {
                        return null;
                    }
                    if (v instanceof String s) {
                        try {
                            return valueOfMethod.invoke(null, s);
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            throw new IllegalStateException(ex);
                        }
                    }
                    throw new IllegalArgumentException();
                };
                return switch (t.structure()) {
                    case SINGLE -> writer;
                    case LIST -> listWriter(writer);
                    case MAP -> mapWriter(writer);
                    case SET -> setWriter(writer);
                };
            } catch (ReflectiveOperationException | SecurityException ex) {
                throw new EntityWriterInitialisationException("Unable to find the getter method ot field for %s".formatted(t.name()), ex);
            }
        } else {
            return Function.identity();
        }
    }
    
    private static Function<Object, Object> listWriter(Function<Object, Object> singleWriter) {
        return vl -> {
            if (vl == null) {
                return null;
            } else if (vl instanceof Collection c) {
                return c.stream().map(singleWriter).toList();
            } else {
                throw new IllegalStateException();
            }
        };
    }
    
    private static Function<Object, Object> setWriter(Function<Object, Object> singleWriter) {
        return vl -> {
            if (vl == null) {
                return null;
            } else if (vl instanceof Collection c) {
                return c.stream().map(singleWriter).collect(Collectors.toSet());
            } else {
                throw new IllegalStateException();
            }
        };
    }
    
    private static Function<Object, Object> mapWriter(Function<Object, Object> singleWriter) {
        return vl -> {
            if (vl == null) {
                return null;
            } else if (vl instanceof Map<?, ?> m) {
                return m.entrySet().stream()
                        .collect(
                                Collectors.toMap(
                                        e -> e.getKey(),
                                        e -> singleWriter.apply(e.getValue())));
            } else {
                throw new IllegalStateException();
            }
        };
    }

    public T write(StructuredObject object) {
        try {
            var arguments = IntStream.range(0, this.constructorParameterGetters.size())
                    .mapToObj(i -> this.constructorParameterGetters.get(i)
                            .andThen(this.constructorParameterValueWriters.get(i))
                            .apply(object))
                    .toArray();
            return this.constructor.newInstance(arguments);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new EntityWriterWriteException("Error writing the source object %s to record class %s"
                    .formatted(object.toString(), this.recordClass.getName()), ex);
        }
    }
}
