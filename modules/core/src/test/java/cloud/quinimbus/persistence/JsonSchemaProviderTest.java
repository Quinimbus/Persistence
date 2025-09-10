package cloud.quinimbus.persistence;

import cloud.quinimbus.persistence.api.schema.EntityTypeBuilder;
import cloud.quinimbus.persistence.api.schema.EntityTypeMigrationBuilder;
import cloud.quinimbus.persistence.api.schema.EntityTypeProperty;
import cloud.quinimbus.persistence.api.schema.SchemaBuilder;
import cloud.quinimbus.persistence.api.schema.migrations.PropertyAddMigrationType;
import cloud.quinimbus.persistence.api.schema.properties.BooleanPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.EnumPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.StringPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.TimestampPropertyType;
import cloud.quinimbus.persistence.schema.json.SingleJsonSchemaProvider;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonSchemaProviderTest {

    @Test
    public void initSingleJsonSchema() throws IOException {
        var schema = SchemaBuilder.builder()
                .id("blog")
                .version(1L)
                .entityTypes(Map.of(
                        "entry",
                        EntityTypeBuilder.builder()
                                .id("entry")
                                .properties(Set.of(
                                        StringPropertyType.propertyBuilder()
                                                .name("title")
                                                .build(),
                                        TimestampPropertyType.propertyBuilder()
                                                .name("created")
                                                .build(),
                                        BooleanPropertyType.propertyBuilder()
                                                .name("published")
                                                .build(),
                                        EnumPropertyType.propertyBuilder(List.of("UNSORTED", "POLITICS", "SPORTS"))
                                                .name("category")
                                                .build(),
                                        StringPropertyType.propertyBuilder()
                                                .name("tags")
                                                .structure(EntityTypeProperty.Structure.LIST)
                                                .build()))
                                .migrations(Set.of(EntityTypeMigrationBuilder.builder()
                                        .name("addCategoryField")
                                        .schemaVersion(1L)
                                        .type(new PropertyAddMigrationType(Map.of("category", "UNSORTED")))
                                        .build()))
                                .build()))
                .build();
        var provider = new SingleJsonSchemaProvider();
        var importedSchema = provider.importSchema(
                new InputStreamReader(this.getClass().getResourceAsStream("JsonSchemaProviderTest_schema.json")));
        System.out.println(importedSchema);
        Assertions.assertEquals(schema, importedSchema);
    }
}
