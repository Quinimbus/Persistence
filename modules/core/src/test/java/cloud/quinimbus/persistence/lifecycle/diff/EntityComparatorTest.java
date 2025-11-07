package cloud.quinimbus.persistence.lifecycle.diff;

import static org.junit.jupiter.api.Assertions.*;

import cloud.quinimbus.persistence.api.schema.EntityTypeBuilder;
import cloud.quinimbus.persistence.api.schema.EntityTypeProperty;
import cloud.quinimbus.persistence.api.schema.properties.StringPropertyType;
import cloud.quinimbus.persistence.entity.DefaultEntity;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class EntityComparatorTest {

    @Test
    public void testCompareString() {
        var type = EntityTypeBuilder.builder()
                .addProperties(StringPropertyType.propertyBuilder()
                        .name("string")
                        .structure(EntityTypeProperty.Structure.SINGLE)
                        .build())
                .build();
        var properties1 = Map.<String, Object>of("string", "Hello World");
        var entity1 = new DefaultEntity<>("first", type, properties1);
        var properties2 = Map.<String, Object>of("string", "Hello Space");
        var entity2 = new DefaultEntity<>("second", type, properties2);
        var diffs = EntityComparator.compareEntities(entity1, entity2);
        assertEquals(1, diffs.size());
    }

    @Test
    public void testCompareList() {
        var type = EntityTypeBuilder.builder()
                .addProperties(StringPropertyType.propertyBuilder()
                        .name("string")
                        .structure(EntityTypeProperty.Structure.LIST)
                        .build())
                .build();
        var properties1 = Map.<String, Object>of("string", List.of("one", "two"));
        var entity1 = new DefaultEntity<>("first", type, properties1);
        var properties2 = Map.<String, Object>of("string", List.of("two", "three"));
        var entity2 = new DefaultEntity<>("second", type, properties2);
        var diffs = EntityComparator.compareEntities(entity1, entity2);
        assertEquals(3, diffs.size());
    }

    @Test
    public void testCompareSet() {
        var type = EntityTypeBuilder.builder()
                .addProperties(StringPropertyType.propertyBuilder()
                        .name("string")
                        .structure(EntityTypeProperty.Structure.SET)
                        .build())
                .build();
        var properties1 = Map.<String, Object>of("string", Set.of("one", "two"));
        var entity1 = new DefaultEntity<>("first", type, properties1);
        var properties2 = Map.<String, Object>of("string", Set.of("two", "three"));
        var entity2 = new DefaultEntity<>("second", type, properties2);
        var diffs = EntityComparator.compareEntities(entity1, entity2);
        assertEquals(3, diffs.size());
    }

    @Test
    public void testCompareMap() {
        var type = EntityTypeBuilder.builder()
                .addProperties(StringPropertyType.propertyBuilder()
                        .name("string")
                        .structure(EntityTypeProperty.Structure.MAP)
                        .build())
                .build();
        var properties1 = Map.<String, Object>of("string", Map.of("a", "one", "b", "two", "c", "three"));
        var entity1 = new DefaultEntity<>("first", type, properties1);
        var properties2 = Map.<String, Object>of("string", Map.of("b", "two.two", "c", "three", "d", "four"));
        var entity2 = new DefaultEntity<>("second", type, properties2);
        var diffs = EntityComparator.compareEntities(entity1, entity2);
        assertEquals(4, diffs.size());
    }
}
