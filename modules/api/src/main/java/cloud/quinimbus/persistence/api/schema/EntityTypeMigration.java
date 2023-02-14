package cloud.quinimbus.persistence.api.schema;

import lombok.Builder;
import lombok.With;

@Builder
@With
public record EntityTypeMigration<T extends EntityTypeMigrationType>(String name, Long schemaVersion, T type) {
    
}
