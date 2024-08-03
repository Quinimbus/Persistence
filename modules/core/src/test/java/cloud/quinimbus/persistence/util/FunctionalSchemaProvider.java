package cloud.quinimbus.persistence.util;

import cloud.quinimbus.config.api.ConfigNode;
import cloud.quinimbus.persistence.api.schema.InvalidSchemaException;
import cloud.quinimbus.persistence.api.schema.PersistenceSchemaProvider;
import cloud.quinimbus.persistence.api.schema.Schema;
import java.util.Map;

public interface FunctionalSchemaProvider extends PersistenceSchemaProvider {
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
