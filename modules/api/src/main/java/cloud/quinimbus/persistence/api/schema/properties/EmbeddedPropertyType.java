package cloud.quinimbus.persistence.api.schema.properties;

import cloud.quinimbus.persistence.api.schema.EntityTypeMigration;
import cloud.quinimbus.persistence.api.schema.EntityTypeProperty;
import cloud.quinimbus.persistence.api.schema.EntityTypePropertyType;
import java.util.Set;

public record EmbeddedPropertyType(Set<EntityTypeProperty> properties, Set<EntityTypeMigration> migrations) implements EntityTypePropertyType {

}
