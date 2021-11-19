package cloud.quinimbus.persistence.api.entity;

import cloud.quinimbus.persistence.api.schema.EntityType;

public interface EmbeddedObject extends StructuredObject {

    EntityType getParentType();

    String[] getPath();
}
