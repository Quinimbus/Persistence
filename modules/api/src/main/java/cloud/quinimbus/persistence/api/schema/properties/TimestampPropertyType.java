package cloud.quinimbus.persistence.api.schema.properties;

import cloud.quinimbus.persistence.api.schema.EntityTypePropertyBuilder;
import cloud.quinimbus.persistence.api.schema.EntityTypePropertyType;

public final record TimestampPropertyType() implements EntityTypePropertyType {

    public static EntityTypePropertyBuilder<TimestampPropertyType> propertyBuilder() {
        return EntityTypePropertyBuilder.<TimestampPropertyType>builder().type(new TimestampPropertyType());
    }
}
