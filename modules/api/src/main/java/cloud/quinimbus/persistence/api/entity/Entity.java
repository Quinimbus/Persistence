package cloud.quinimbus.persistence.api.entity;

import cloud.quinimbus.persistence.api.schema.EntityType;

public interface Entity<K> extends StructuredObject {

    K getId();

    EntityType getType();
}
