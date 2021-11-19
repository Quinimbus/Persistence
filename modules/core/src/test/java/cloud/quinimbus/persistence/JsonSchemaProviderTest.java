package cloud.quinimbus.persistence;

import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.EntityTypeProperty;
import cloud.quinimbus.persistence.api.schema.Schema;
import cloud.quinimbus.persistence.api.schema.properties.BooleanPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.EnumPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.StringPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.TimestampPropertyType;
import cloud.quinimbus.persistence.schema.json.SingleJsonSchemaProvider;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonSchemaProviderTest {

    @Test
    public void initSingleJsonSchema() throws IOException {
        var schema = Schema.builder()
                .id("blog")
                .version(1L)
                .entityTypes(Map.of(
                        "entry",
                        EntityType.builder()
                                .id("entry")
                                .properties(Set.of(
                                        EntityTypeProperty.builder()
                                                .name("title")
                                                .type(new StringPropertyType())
                                                .structure(EntityTypeProperty.Structure.SINGLE)
                                                .build(),
                                        EntityTypeProperty.builder()
                                                .name("created")
                                                .type(new TimestampPropertyType())
                                                .structure(EntityTypeProperty.Structure.SINGLE)
                                                .build(),
                                        EntityTypeProperty.builder()
                                                .name("published")
                                                .type(new BooleanPropertyType())
                                                .structure(EntityTypeProperty.Structure.SINGLE)
                                                .build(),
                                        EntityTypeProperty.builder()
                                                .name("category")
                                                .type(new EnumPropertyType(List.of("POLITICS", "SPORTS")))
                                                .structure(EntityTypeProperty.Structure.SINGLE)
                                                .build(),
                                        EntityTypeProperty.builder()
                                                .name("tags")
                                                .type(new StringPropertyType())
                                                .structure(EntityTypeProperty.Structure.LIST)
                                                .build()))
                                .build()))
                .build();
        var provider = new SingleJsonSchemaProvider();
        provider.importSchema(this.getClass().getResourceAsStream("JsonSchemaProviderTest_schema.json"));
        System.out.println(provider.getSchemas());
        Assertions.assertEquals(schema, provider.getSchemas().stream().findFirst().get());
    }
}
