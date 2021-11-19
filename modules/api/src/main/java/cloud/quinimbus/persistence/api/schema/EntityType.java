package cloud.quinimbus.persistence.api.schema;

import java.util.Set;
import lombok.Builder;
import lombok.With;

@Builder
@With
public record EntityType(String id, Set<EntityTypeProperty> properties) {

}
