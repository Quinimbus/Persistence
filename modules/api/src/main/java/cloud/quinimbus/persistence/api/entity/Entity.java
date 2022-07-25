package cloud.quinimbus.persistence.api.entity;

import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.EntityTypePropertyType;

public interface Entity<K> extends StructuredObject<EntityTypePropertyType> {

    K getId();

    EntityType getType();
}
