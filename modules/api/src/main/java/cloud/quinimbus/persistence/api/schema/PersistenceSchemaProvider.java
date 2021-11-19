package cloud.quinimbus.persistence.api.schema;

import java.util.Set;

@FunctionalInterface
public interface PersistenceSchemaProvider {

    public Set<Schema> getSchemas();
}
