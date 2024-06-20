package cloud.quinimbus.persistence.api.schema;

import cloud.quinimbus.persistence.api.schema.properties.EmbeddedPropertyType;
import java.util.Optional;
import java.util.Set;
import lombok.Builder;
import lombok.With;

@Builder
@With
public record EntityType(
        String id,
        Optional<OwningEntityTypeRef> owningEntity,
        Set<EntityTypeProperty> properties,
        Set<EntityTypeMigration> migrations)
        implements StructuredObjectType {

    public static record OwningEntityTypeRef(String id, String field) {}

    public Optional<EntityTypeProperty> property(String name) {
        return this.properties().stream().filter(etp -> etp.name().equals(name)).findAny();
    }

    public Optional<EmbeddedPropertyType> embeddedPropertyType(String propertyName) {
        return this.property(propertyName)
                .map(EntityTypeProperty::type)
                .filter(EmbeddedPropertyType.class::isInstance)
                .map(EmbeddedPropertyType.class::cast);
    }
}
