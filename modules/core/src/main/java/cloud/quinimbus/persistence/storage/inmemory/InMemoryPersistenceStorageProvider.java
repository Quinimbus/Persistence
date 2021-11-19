package cloud.quinimbus.persistence.storage.inmemory;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.schema.Schema;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorage;
import cloud.quinimbus.persistence.api.storage.PersistenceStorageProvider;

public class InMemoryPersistenceStorageProvider implements PersistenceStorageProvider {

    @Override
    public PersistenceSchemaStorage createSchema(PersistenceContext context, Schema schema) {
        return new InMemorySchemaStorage(context, schema);
    }
}
