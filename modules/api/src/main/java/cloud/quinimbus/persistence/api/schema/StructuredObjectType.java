package cloud.quinimbus.persistence.api.schema;

import cloud.quinimbus.persistence.api.schema.properties.EmbeddedPropertyType;
import java.util.Optional;

public interface StructuredObjectType {

    Optional<EntityTypeProperty> property(String name);

    default Optional<EntityTypePropertyType> propertyType(String name) {
        return this.property(name).map(EntityTypeProperty::type);
    }

    default Optional<EmbeddedPropertyType> embeddedPropertyType(String propertyName) {
        return this.propertyType(propertyName)
                .filter(EmbeddedPropertyType.class::isInstance)
                .map(EmbeddedPropertyType.class::cast);
    }
}
