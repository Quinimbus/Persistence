package cloud.quinimbus.persistence.storage.inmemory;

import cloud.quinimbus.common.annotations.Provider;
import cloud.quinimbus.config.api.ConfigNode;
import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.storage.PersistenceStorageProvider;
import java.util.Map;
import java.util.Optional;
import static java.util.function.Predicate.not;

@Provider(name = "In Memory Dummy persistence storage provider", alias = "memory", priority = 0)
public class InMemoryPersistenceStorageProvider implements PersistenceStorageProvider<InMemorySchemaStorage> {

    public InMemorySchemaStorage createSchema(PersistenceContext context, String schema) throws PersistenceException {
        return new InMemorySchemaStorage(
                context,
                context.getSchema(schema).orElseThrow(() -> new PersistenceException("Schema %s not found".formatted(schema))));
    }

    @Override
    public InMemorySchemaStorage createSchema(PersistenceContext context, Map<String, Object> params) throws PersistenceException {
        var schema = Optional.ofNullable(params.get("schema"))
                .filter(s -> s instanceof String str && !str.isEmpty())
                .map(s -> (String) s)
                .orElseThrow(() -> new PersistenceException("Missing or invalid parameter: schema"));
        return this.createSchema(context, schema);
    }

    @Override
    public InMemorySchemaStorage createSchema(PersistenceContext context, ConfigNode config) throws PersistenceException {
        var schema = config.asString("schema")
                .filter(not(String::isEmpty))
                .map(s -> (String) s)
                .orElseThrow(() -> new PersistenceException("Missing configuration value: schema"));
        return this.createSchema(context, schema);
    }
}
