package cloud.quinimbus.persistence.api.schema;

import java.time.Instant;
import lombok.Builder;
import lombok.With;

@Builder
@With
public record Metadata(String id, Long version, Instant creationTime) {
    
}
