package cloud.quinimbus.persistence.schema.json;

import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.EntityTypeMigration;
import cloud.quinimbus.persistence.api.schema.EntityTypeMigrationType;
import cloud.quinimbus.persistence.api.schema.EntityTypeProperty;
import cloud.quinimbus.persistence.api.schema.EntityTypePropertyType;
import cloud.quinimbus.persistence.api.schema.PersistenceSchemaProvider;
import cloud.quinimbus.persistence.api.schema.migrations.PropertyAddMigrationType;
import cloud.quinimbus.persistence.api.schema.properties.BooleanPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.EmbeddedPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.EnumPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.IntegerPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.LocalDatePropertyType;
import cloud.quinimbus.persistence.api.schema.properties.StringPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.TimestampPropertyType;
import cloud.quinimbus.tools.throwing.ThrowingOptional;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import name.falgout.jeffrey.throwing.stream.ThrowingStream;

public abstract class AbstractJsonSchemaProvider implements PersistenceSchemaProvider {
    
    public static record EntityTypeMixin(@JsonIgnore Set<EntityTypeProperty> properties, @JsonIgnore Set<EntityTypeMigration> migrations) {}

    protected Map<String, EntityType> importTypes(ObjectMapper mapper, JsonNode node) throws IOException {
        return ThrowingStream
                .of(StreamSupport.stream(Spliterators.spliteratorUnknownSize(node.fields(), 0), false), IOException.class)
                .map(e -> this.importType(e.getKey(), mapper, (ObjectNode) e.getValue()))
                .collect(Collectors.toMap(et -> et.id(), et -> et));
    }

    protected EntityType importType(String id, ObjectMapper mapper, ObjectNode node) throws IOException {
        mapper.addMixIn(EntityType.class, EntityTypeMixin.class);
        var deserializationModule = new SimpleModule();
        deserializationModule.addDeserializer(EntityTypePropertyType.class, new EntityTypePropertyTypeDeserializer());
        deserializationModule.addDeserializer(EntityTypeMigrationType.class, new EntityTypeMigrationTypeDeserializer());
        mapper.registerModule(deserializationModule);
        return mapper.treeToValue(node, EntityType.class)
                .withProperties(this.importProperties(mapper, node))
                .withMigrations(this.importMigrations(mapper, node))
                .withId(id);
    }

    private Set<EntityTypeProperty> importProperties(ObjectCodec codec, ObjectNode node) throws IOException {
        return ThrowingOptional.ofNullable(node.get("properties"), IOException.class)
                .map(n -> ThrowingStream.of(StreamSupport
                        .stream(Spliterators.spliteratorUnknownSize(n.fields(), 0), false), IOException.class)
                        .map(e -> readProperty(codec, e))
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
    }
    
    private EntityTypeProperty readProperty(ObjectCodec codec, Entry<String, JsonNode> entry) throws IOException {
        var property = entry.getValue().traverse(codec).readValuesAs(EntityTypeProperty.class).next()
        //var property = mapper.treeToValue(entry.getValue(), EntityTypeProperty.class)
                .withName(entry.getKey());
        if (property.structure() == null) {
            property = property.withStructure(EntityTypeProperty.Structure.SINGLE);
        }
        return property;
    }

    private Set<EntityTypeMigration> importMigrations(ObjectCodec codec, ObjectNode node) throws IOException {
        return ThrowingOptional.ofNullable(node.get("migrations"), IOException.class)
                .map(n -> ThrowingStream.of(StreamSupport
                        .stream(Spliterators.spliteratorUnknownSize(n.fields(), 0), false), IOException.class)
                        .map(e -> readMigration(codec, e))
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
    }
    
    private EntityTypeMigration readMigration(ObjectCodec codec, Entry<String, JsonNode> entry) throws IOException {
        var migration = entry.getValue().traverse(codec).readValuesAs(EntityTypeMigration.class).next()
                .withName(entry.getKey());
        return migration;
    }
    
    private class EntityTypePropertyTypeDeserializer extends JsonDeserializer<EntityTypePropertyType> {

        @Override
        public EntityTypePropertyType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            var node = p.getCodec().readTree(p);
            if (node instanceof ValueNode vn) {
                var type = vn.asText();
                return switch (type) {
                    case "STRING" -> new StringPropertyType();
                    case "BOOLEAN" -> new BooleanPropertyType();
                    case "TIMESTAMP" -> new TimestampPropertyType();
                    case "LOCALDATE" -> new LocalDatePropertyType();
                    case "INTEGER" -> new IntegerPropertyType();
                    default -> throw new IllegalStateException("Unknown simple property type in json: " + type);
                };
            } else if (node instanceof ObjectNode on) {
                if (on.has("ENUM")) {
                    var en = on.get("ENUM");
                    if (en instanceof ArrayNode an) {
                        var allowedValues = StreamSupport.stream(an.spliterator(), false).map(n -> n.asText()).toList();
                        return new EnumPropertyType(allowedValues);
                    } else {
                        throw new IllegalStateException();
                    }
                } else if (on.has("EMBEDDED")) {
                    var en = on.get("EMBEDDED");
                    if (en instanceof ObjectNode eon) {
                        var properties = AbstractJsonSchemaProvider.this.importProperties(ctxt.getParser().getCodec(), eon);
                        var migrations = AbstractJsonSchemaProvider.this.importMigrations(ctxt.getParser().getCodec(), eon);
                        return new EmbeddedPropertyType(properties, migrations);
                    } else {
                        throw new IllegalStateException();
                    }
                } else {
                    throw new IllegalStateException();
                }
            } else {
                throw new IllegalStateException();
            }
        }
    }
    
    private class EntityTypeMigrationTypeDeserializer extends JsonDeserializer<EntityTypeMigrationType> {

        @Override
        public EntityTypeMigrationType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            var node = p.getCodec().readTree(p);
            if (node instanceof ObjectNode on) {
                if (on.has("ADD_PROPERTIES")) {
                    var propertiesNode = on.get("ADD_PROPERTIES");
                    if (propertiesNode instanceof ObjectNode propertiesObjectNode) {
                        return new PropertyAddMigrationType(new ObjectMapper().convertValue(propertiesObjectNode, new TypeReference<Map<String, Object>>() {
                        }));
                    } else {
                        throw new IllegalStateException();
                    }
                } else {
                    throw new IllegalStateException();
                }
            } else {
                throw new IllegalStateException();
            }
        }
    }
}
