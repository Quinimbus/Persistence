package cloud.quinimbus.persistence;

import cloud.quinimbus.config.api.ConfigNode;
import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.InvalidSchemaException;
import cloud.quinimbus.persistence.api.schema.PersistenceSchemaProvider;
import cloud.quinimbus.persistence.api.schema.Schema;
import cloud.quinimbus.persistence.storage.inmemory.InMemoryPersistenceStorageProvider;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class SchemaTest {

    @FunctionalInterface
    public static interface FunctionalSchemaProvider extends PersistenceSchemaProvider {

        Schema importSchema();

        @Override
        default Schema loadSchema(ConfigNode node) throws InvalidSchemaException {
            return this.importSchema();
        }

        @Override
        default Schema loadSchema(Map<String, Object> params) throws InvalidSchemaException {
            return this.importSchema();
        }
    }

    @Test
    public void initSchema() throws PersistenceException, InvalidSchemaException {
        var persistenceContext = new PersistenceContextImpl();
        FunctionalSchemaProvider schemaProvider = () -> Schema.builder()
                .id("SchemaTest")
                .entityTypes(Map.of("", EntityType.builder().build()))
                .build();
        persistenceContext.importSchema(schemaProvider);
        var storage = new InMemoryPersistenceStorageProvider();
        storage.createSchema(persistenceContext, Map.of("schema", "SchemaTest"));
    }
}
