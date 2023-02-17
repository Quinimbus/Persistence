package cloud.quinimbus.persistence.api.schema;

import java.util.Optional;

public interface StructuredObjectType {
    
    Optional<EntityTypeProperty> property(String name);
}
