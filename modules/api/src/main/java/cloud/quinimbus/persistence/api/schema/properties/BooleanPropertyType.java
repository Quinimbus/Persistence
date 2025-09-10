package cloud.quinimbus.persistence.api.schema.properties;

import cloud.quinimbus.persistence.api.schema.EntityTypePropertyBuilder;
import cloud.quinimbus.persistence.api.schema.EntityTypePropertyType;

public final record BooleanPropertyType() implements EntityTypePropertyType {

    public static EntityTypePropertyBuilder<BooleanPropertyType> propertyBuilder() {
        return EntityTypePropertyBuilder.<BooleanPropertyType>builder().type(new BooleanPropertyType());
    }
}
