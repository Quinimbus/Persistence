package cloud.quinimbus.persistence.api.schema;

import cloud.quinimbus.config.api.ConfigNode;
import java.util.Map;

public interface PersistenceSchemaProvider {

    Schema loadSchema(Map<String, Object> params) throws InvalidSchemaException;

    Schema loadSchema(ConfigNode node) throws InvalidSchemaException;
}
