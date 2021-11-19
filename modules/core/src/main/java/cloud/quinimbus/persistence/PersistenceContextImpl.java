package cloud.quinimbus.persistence;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.entity.EmbeddedObject;
import cloud.quinimbus.persistence.api.entity.Entity;
import cloud.quinimbus.persistence.api.entity.EntityReaderInitialisationException;
import cloud.quinimbus.persistence.api.entity.EntityWriterInitialisationException;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.PersistenceSchemaProvider;
import cloud.quinimbus.persistence.entity.DefaultEntity;
import cloud.quinimbus.persistence.api.schema.InvalidSchemaException;
import cloud.quinimbus.persistence.api.schema.Schema;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorage;
import cloud.quinimbus.persistence.entity.reader.RecordEntityReader;
import cloud.quinimbus.persistence.entity.writer.RecordEntityWriter;
import cloud.quinimbus.persistence.api.entity.UnparseableValueException;
import cloud.quinimbus.persistence.api.schema.EntityTypeProperty;
import cloud.quinimbus.persistence.api.schema.properties.BooleanPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.EmbeddedPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.EnumPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.IntegerPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.StringPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.TimestampPropertyType;
import cloud.quinimbus.persistence.entity.DefaultEmbeddedObject;
import cloud.quinimbus.persistence.parsers.BooleanParser;
import cloud.quinimbus.persistence.parsers.EmbeddedParser;
import cloud.quinimbus.persistence.parsers.EnumParser;
import cloud.quinimbus.persistence.parsers.IntegerParser;
import cloud.quinimbus.persistence.parsers.StringParser;
import cloud.quinimbus.persistence.parsers.TimestampParser;
import cloud.quinimbus.persistence.parsers.ValueParser;
import cloud.quinimbus.persistence.schema.json.SingleJsonSchemaProvider;
import cloud.quinimbus.persistence.schema.record.RecordSchemaProvider;
import cloud.quinimbus.persistence.storage.inmemory.InMemorySchemaStorage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import name.falgout.jeffrey.throwing.stream.ThrowingStream;

public class PersistenceContextImpl implements PersistenceContext {

    private final Map<String, Schema> schemas;

    private final Map<String, PersistenceSchemaStorage> schemaStorages;

    public PersistenceContextImpl() {
        this.schemas = new LinkedHashMap<>();
        this.schemaStorages = new LinkedHashMap<>();
    }

    @Override
    public Optional<Schema> getSchema(String id) {
        return Optional.ofNullable(this.schemas.get(id));
    }

    @Override
    public Optional<PersistenceSchemaStorage> getSchemaStorage(String id) {
        return Optional.ofNullable(this.schemaStorages.get(id));
    }

    @Override
    public void setSchemaStorage(String id, PersistenceSchemaStorage storage) {
        this.schemaStorages.put(id, storage);
    }

    @Override
    public void setInMemorySchemaStorage(String id) {
        this.schemaStorages.put(id, new InMemorySchemaStorage(this, this.schemas.get(id)));
    }

    @Override
    public <K> Entity<K> newEntity(K id, EntityType type) {
        return new DefaultEntity<>(id, type);
    }

    @Override
    public <K> Entity<K> newEntity(K id, EntityType type, Map<String, Object> properties) throws UnparseableValueException {
        var typeProperties = type.properties().stream().collect(Collectors.toMap(pt -> pt.name(), pt -> pt));
        Map<String, Object> parsedProperties = ThrowingStream
                .of(properties.entrySet().stream(), UnparseableValueException.class)
                .filter(e -> typeProperties.containsKey(e.getKey()))
                .map(e -> Map.entry(
                        e.getKey(),
                        this.parse(type, List.of(), typeProperties.get(e.getKey()), e)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new DefaultEntity<>(id, type, parsedProperties);
    }

    @Override
    public EmbeddedObject newEmbedded(EmbeddedPropertyType type, EntityType parentType, List<String> path, Map<String, Object> properties) throws UnparseableValueException {
        var typeProperties = type.properties().stream().collect(Collectors.toMap(pt -> pt.name(), pt -> pt));
        Map<String, Object> parsedProperties = ThrowingStream
                .of(properties.entrySet().stream(), UnparseableValueException.class)
                .filter(e -> typeProperties.containsKey(e.getKey()))
                .map(e -> Map.entry(
                        e.getKey(),
                        this.parse(parentType, path, typeProperties.get(e.getKey()), e)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new DefaultEmbeddedObject(path.toArray(new String[]{}), parentType, parsedProperties);
    }

    @Override
    public PersistenceSchemaProvider importSchemaFromSingleJson(InputStream inputStream) throws IOException {
        var provider = new SingleJsonSchemaProvider();
        provider.importSchema(inputStream);
        provider.getSchemas().stream().forEach(s -> schemas.put(s.id(), s));
        return provider;
    }

    @Override
    public PersistenceSchemaProvider importRecordSchema(Class<? extends Record>... recordClasses) throws InvalidSchemaException {
        var provider = new RecordSchemaProvider();
        provider.importSchema(recordClasses);
        provider.getSchemas().stream().forEach(s -> schemas.put(s.id(), s));
        return provider;
    }

    @Override
    public <T extends Record> RecordEntityReader<T> getRecordEntityReader(EntityType type, Class<T> recordClass) throws EntityReaderInitialisationException {
        if (!recordClass.isRecord()) {
            throw new IllegalArgumentException("Type %s is no record".formatted(recordClass.getName()));
        }
        return new RecordEntityReader(type, recordClass);
    }

    @Override
    public <T extends Record> RecordEntityWriter<T> getRecordEntityWriter(EntityType type, Class<T> recordClass) throws EntityWriterInitialisationException {
        if (!recordClass.isRecord()) {
            throw new IllegalArgumentException("Type %s is no record".formatted(recordClass.getName()));
        }
        return new RecordEntityWriter(type, recordClass);
    }

    private Object parse(EntityType parentType, List<String> parentPath, EntityTypeProperty property, Map.Entry<String, Object> e) throws UnparseableValueException {
        var type = property.type();
        if (type == null) {
            throw new IllegalStateException("type not found");
        }
        var parser = this.getParser(parentType, parentPath, property);
        return switch (property.structure()) {
            case SINGLE ->
                parser.parse(e.getValue());
            case LIST -> {
                if (e.getValue() instanceof Collection<?> col) {
                    yield ThrowingStream.of(col.stream(), UnparseableValueException.class)
                    .map(parser::parse)
                    .collect(Collectors.toList());
                }
                throw new UnparseableValueException(
                        "Cannot read a value of type %s as list structure".formatted(e.getValue().getClass()));
            }
            case SET -> {
                if (e.getValue() instanceof Collection<?> col) {
                    yield ThrowingStream.of(col.stream(), UnparseableValueException.class)
                    .map(parser::parse)
                    .collect(Collectors.toSet());
                }
                throw new UnparseableValueException(
                        "Cannot read a value of type %s as set structure".formatted(e.getValue().getClass()));
            }
        };
    }

    private ValueParser getParser(EntityType parentType, List<String> parentPath, EntityTypeProperty property) {
        var type = property.type();
        if (type instanceof StringPropertyType) {
            return new StringParser();
        } else if (type instanceof BooleanPropertyType) {
            return new BooleanParser();
        } else if (type instanceof TimestampPropertyType) {
            return new TimestampParser();
        } else if (type instanceof EnumPropertyType ept) {
            return new EnumParser(ept.allowedValues());
        } else if (type instanceof IntegerPropertyType) {
            return new IntegerParser();
        } else if (type instanceof EmbeddedPropertyType ept) {
            var newParentPath = new ArrayList<>(parentPath);
            newParentPath.add(property.name());
            return new EmbeddedParser(
                    ept,
                    newParentPath,
                    parentType,
                    this);
        } else {
            throw new IllegalStateException(); // Cannot happen
        }
    }
}
