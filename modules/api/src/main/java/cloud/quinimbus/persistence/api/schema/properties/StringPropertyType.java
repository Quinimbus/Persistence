package cloud.quinimbus.persistence.api.schema.properties;

import cloud.quinimbus.persistence.api.schema.EntityTypePropertyBuilder;
import cloud.quinimbus.persistence.api.schema.EntityTypePropertyType;

public final record StringPropertyType() implements EntityTypePropertyType {

    public static EntityTypePropertyBuilder<StringPropertyType> propertyBuilder() {
        return EntityTypePropertyBuilder.<StringPropertyType>builder().type(new StringPropertyType());
    }
}
