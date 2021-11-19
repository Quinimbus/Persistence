package cloud.quinimbus.persistence.storage.mongo;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.schema.Schema;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorage;
import cloud.quinimbus.persistence.api.storage.PersistenceStorageProvider;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class MongoPersistenceStorageProvider implements PersistenceStorageProvider {

    private final MongoClient client;

    public MongoPersistenceStorageProvider(String host, String username, String password) {
        this.client = MongoClients.create("mongodb://%s:%s@%s".formatted(
                username,
                password,
                host));
    }

    @Override
    public PersistenceSchemaStorage createSchema(PersistenceContext context, Schema schema) {
        return new MongoSchemaStorage(this.client, schema, context);
    }
}
