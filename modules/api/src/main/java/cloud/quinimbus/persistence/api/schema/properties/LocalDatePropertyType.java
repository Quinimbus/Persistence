package cloud.quinimbus.persistence.api.schema.properties;

import cloud.quinimbus.persistence.api.schema.EntityTypePropertyBuilder;
import cloud.quinimbus.persistence.api.schema.EntityTypePropertyType;

public final record LocalDatePropertyType() implements EntityTypePropertyType {

    public static EntityTypePropertyBuilder<LocalDatePropertyType> propertyBuilder() {
        return EntityTypePropertyBuilder.<LocalDatePropertyType>builder().type(new LocalDatePropertyType());
    }
}
