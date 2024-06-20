package cloud.quinimbus.persistence.storage.mongo;

import cloud.quinimbus.common.annotations.Provider;
import cloud.quinimbus.config.api.ConfigNode;
import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.storage.PersistenceStorageProvider;
import com.mongodb.client.MongoClients;
import java.util.Map;
import java.util.Optional;

@Provider(name = "MongoDB storage provider", alias = "mongo", priority = 0)
public class MongoPersistenceStorageProvider implements PersistenceStorageProvider<MongoSchemaStorage> {

    public MongoSchemaStorage createSchema(
            PersistenceContext context, String schema, String username, String password, String host)
            throws PersistenceException {
        var client = MongoClients.create("mongodb://%s:%s@%s".formatted(username, password, host));
        return new MongoSchemaStorage(
                client,
                context.getSchema(schema)
                        .orElseThrow(() -> new PersistenceException("Schema %s not found".formatted(schema))),
                context);
    }

    @Override
    public MongoSchemaStorage createSchema(PersistenceContext context, Map<String, Object> params)
            throws PersistenceException {
        var schema = Optional.ofNullable(params.get("schema"))
                .filter(p -> p instanceof String)
                .map(s -> (String) s)
                .orElseThrow(() -> new PersistenceException("Missing or invalid parameter: schema"));
        var username = Optional.ofNullable(params.get("username"))
                .filter(p -> p instanceof String)
                .map(s -> (String) s)
                .orElseThrow(() -> new PersistenceException("Missing or invalid parameter: username"));
        var password = Optional.ofNullable(params.get("password"))
                .filter(p -> p instanceof String)
                .map(s -> (String) s)
                .orElseThrow(() -> new PersistenceException("Missing or invalid parameter: password"));
        var host = Optional.ofNullable(params.get("host"))
                .filter(p -> p instanceof String)
                .map(s -> (String) s)
                .orElseThrow(() -> new PersistenceException("Missing or invalid parameter: host"));
        return this.createSchema(context, schema, username, password, host);
    }

    @Override
    public MongoSchemaStorage createSchema(PersistenceContext context, ConfigNode config) throws PersistenceException {
        var schema = config.asString("schema")
                .orElseThrow(() -> new PersistenceException("Missing or invalid parameter: schema"));
        var username = config.asString("username")
                .orElseThrow(() -> new PersistenceException("Missing or invalid parameter: username"));
        var password = config.asString("password")
                .orElseThrow(() -> new PersistenceException("Missing or invalid parameter: password"));
        var host = config.asString("host")
                .orElseThrow(() -> new PersistenceException("Missing or invalid parameter: host"));
        return this.createSchema(context, schema, username, password, host);
    }
}
