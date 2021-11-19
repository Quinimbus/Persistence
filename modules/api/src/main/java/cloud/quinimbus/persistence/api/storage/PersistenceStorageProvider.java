package cloud.quinimbus.persistence.api.storage;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.schema.Schema;

public interface PersistenceStorageProvider {

    PersistenceSchemaStorage createSchema(PersistenceContext context, Schema schema);
}
