package cloud.quinimbus.persistence.api.schema.properties;

import cloud.quinimbus.persistence.api.entity.EmbeddedPropertyHandler;
import cloud.quinimbus.persistence.api.schema.EntityTypeMigration;
import cloud.quinimbus.persistence.api.schema.EntityTypeProperty;
import cloud.quinimbus.persistence.api.schema.EntityTypePropertyType;
import cloud.quinimbus.persistence.api.schema.StructuredObjectType;
import java.util.Optional;
import java.util.Set;

public record EmbeddedPropertyType(
        Set<EntityTypeProperty> properties,
        Set<EntityTypeMigration> migrations,
        Class<? extends EmbeddedPropertyHandler> handlerClass)
        implements EntityTypePropertyType, StructuredObjectType {

    @Override
    public Optional<EntityTypeProperty> property(String name) {
        return this.properties().stream().filter(etp -> etp.name().equals(name)).findAny();
    }
}
