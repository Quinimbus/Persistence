package cloud.quinimbus.persistence.api.schema.properties;

import cloud.quinimbus.persistence.api.schema.EntityTypePropertyBuilder;
import cloud.quinimbus.persistence.api.schema.EntityTypePropertyType;
import java.util.List;

public final record EnumPropertyType(List<String> allowedValues) implements EntityTypePropertyType {

    public static EntityTypePropertyBuilder<EnumPropertyType> propertyBuilder(List<String> allowedValues) {
        return EntityTypePropertyBuilder.<EnumPropertyType>builder().type(new EnumPropertyType(allowedValues));
    }
}
