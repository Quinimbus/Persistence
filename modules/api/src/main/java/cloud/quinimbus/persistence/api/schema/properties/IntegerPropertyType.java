package cloud.quinimbus.persistence.api.schema.properties;

import cloud.quinimbus.persistence.api.schema.EntityTypePropertyBuilder;
import cloud.quinimbus.persistence.api.schema.EntityTypePropertyType;

public final record IntegerPropertyType() implements EntityTypePropertyType {

    public static EntityTypePropertyBuilder<IntegerPropertyType> propertyBuilder() {
        return EntityTypePropertyBuilder.<IntegerPropertyType>builder().type(new IntegerPropertyType());
    }
}
