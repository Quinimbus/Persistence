package cloud.quinimbus.persistence.api.schema;

import java.util.Map;
import lombok.Builder;

@Builder
public record Schema(String id, Map<String, EntityType> entityTypes, Long version) {

}
