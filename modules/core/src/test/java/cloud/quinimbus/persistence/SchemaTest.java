package cloud.quinimbus.persistence;

import cloud.quinimbus.persistence.storage.inmemory.InMemoryPersistenceStorageProvider;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.PersistenceSchemaProvider;
import cloud.quinimbus.persistence.api.schema.Schema;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class SchemaTest {

    @Test
    public void initSchema() {
        PersistenceSchemaProvider schemaProvider = () -> Set.of(
                Schema.builder()
                        .entityTypes(Map.of(
                                "",
                                EntityType.builder()
                                        .build()))
                        .build()
        );
        var storage = new InMemoryPersistenceStorageProvider();
        schemaProvider.getSchemas().forEach(s -> storage.createSchema(null, s));
    }
}
