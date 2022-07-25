package cloud.quinimbus.persistence.api.schema;

import java.util.Optional;
import java.util.Set;
import lombok.Builder;
import lombok.With;

@Builder
@With
public record EntityType(String id, Set<EntityTypeProperty> properties) {

    public Optional<EntityTypeProperty> property(String name) {
        return this.properties().stream()
                        .filter(etp -> etp.name().equals(name))
                        .findAny();
    }
}
