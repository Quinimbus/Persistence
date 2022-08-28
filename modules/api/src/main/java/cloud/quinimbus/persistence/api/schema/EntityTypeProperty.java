package cloud.quinimbus.persistence.api.schema;

import lombok.Builder;
import lombok.With;

@Builder
@With
public record EntityTypeProperty<T extends EntityTypePropertyType>(String name, T type, Structure structure) {

    public static enum Structure {
        SINGLE, LIST, SET, MAP
    }
}
