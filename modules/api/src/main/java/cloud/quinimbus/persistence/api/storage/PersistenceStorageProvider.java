package cloud.quinimbus.persistence.api.storage;

import cloud.quinimbus.config.api.ConfigNode;
import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.PersistenceException;
import java.util.Map;

public interface PersistenceStorageProvider<T extends PersistenceSchemaStorage> {

    T createSchema(PersistenceContext context, Map<String, Object> params) throws PersistenceException;

    T createSchema(PersistenceContext context, ConfigNode config) throws PersistenceException;
}
