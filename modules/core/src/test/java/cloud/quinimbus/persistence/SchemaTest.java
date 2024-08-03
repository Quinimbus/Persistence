package cloud.quinimbus.persistence;

import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.schema.EntityTypeBuilder;
import cloud.quinimbus.persistence.api.schema.InvalidSchemaException;
import cloud.quinimbus.persistence.api.schema.SchemaBuilder;
import cloud.quinimbus.persistence.storage.inmemory.InMemoryPersistenceStorageProvider;
import cloud.quinimbus.persistence.util.FunctionalSchemaProvider;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class SchemaTest {

    @Test
    public void initSchema() throws PersistenceException, InvalidSchemaException {
        var persistenceContext = new PersistenceContextImpl();
        FunctionalSchemaProvider schemaProvider = () -> SchemaBuilder.builder()
                .id("SchemaTest")
                .entityTypes(Map.of(
                        "myEntity",
                        EntityTypeBuilder.builder().properties(Set.of()).build()))
                .build();
        persistenceContext.importSchema(schemaProvider);
        var storage = new InMemoryPersistenceStorageProvider();
        storage.createSchema(persistenceContext, Map.of("schema", "SchemaTest"));
    }
}
