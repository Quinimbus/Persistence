package cloud.quinimbus.persistence.api.entity;

import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.EntityTypePropertyType;

public interface EmbeddedObject extends StructuredObject<EntityTypePropertyType> {

    EntityType getParentType();

    String[] getPath();
}
