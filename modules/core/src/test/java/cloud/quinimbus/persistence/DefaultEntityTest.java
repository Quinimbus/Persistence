package cloud.quinimbus.persistence;

import cloud.quinimbus.persistence.api.entity.EmbeddedObject;
import cloud.quinimbus.persistence.api.entity.Entity;
import cloud.quinimbus.persistence.api.entity.StructuredObjectEntry;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.EntityTypeProperty;
import cloud.quinimbus.persistence.api.schema.EntityTypePropertyType;
import cloud.quinimbus.persistence.api.schema.properties.EmbeddedPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.IntegerPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.StringPropertyType;
import cloud.quinimbus.persistence.entity.DefaultEmbeddedObject;
import cloud.quinimbus.persistence.entity.DefaultEntity;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class DefaultEntityTest {

    @Test
    public void testSimpleAsBasicMap() {
        var entity = this.createTestEntity();
        assertEquals(
                Map.of(
                        "string", "Hello World",
                        "stringlist", List.of("a", "b"),
                        "number", 13,
                        "embedded", Map.of("string", "embedded")),
                entity.asBasicMap());
    }

    @Test
    public void testAsBasicMapWithToStringConverter() {
        var entity = this.createTestEntity();
        assertEquals(
                Map.of(
                        "string", "Hello World",
                        "stringlist", List.of("a", "b"),
                        "number", "13",
                        "embedded", Map.of("string", "embedded")),
                entity.asBasicMap(this::convertToString));
    }

    private Entity<String> createTestEntity() {
        var embeddedType = new EmbeddedPropertyType(Set.of(
                new EntityTypeProperty("string", new StringPropertyType(), EntityTypeProperty.Structure.SINGLE)));
        var type = new EntityType("testEntity", Set.of(
                new EntityTypeProperty<>("string", new StringPropertyType(), EntityTypeProperty.Structure.SINGLE),
                new EntityTypeProperty<>("stringlist", new StringPropertyType(), EntityTypeProperty.Structure.LIST),
                new EntityTypeProperty<>("number", new IntegerPropertyType(), EntityTypeProperty.Structure.SINGLE),
                new EntityTypeProperty<>("embedded", embeddedType, EntityTypeProperty.Structure.SINGLE)));
        var properties = Map.<String, Object>of(
                "string", "Hello World",
                "stringlist", List.of("a", "b"),
                "number", 13,
                "embedded", new DefaultEmbeddedObject(
                        new String[]{"embedded"},
                        type,
                        Map.of("string", "embedded"),
                        embeddedType));
        return new DefaultEntity<>("first", type, properties);
    }

    private Object convertToString(StructuredObjectEntry<EntityTypePropertyType> entry) {
        if (entry.value() == null) {
            return "<null>";
        }
        if (entry.type() instanceof EmbeddedPropertyType) {
            return ((EmbeddedObject) entry.value()).asBasicMap(this::convertToString);
        }
        return entry.value().toString();
    }
}
