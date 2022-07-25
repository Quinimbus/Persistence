package cloud.quinimbus.persistence.entity;

import cloud.quinimbus.persistence.api.entity.StructuredObjectEntry;
import cloud.quinimbus.persistence.api.schema.EntityTypePropertyType;

public record DefaultEntityPropertyEntry(String key, Object value, EntityTypePropertyType type, boolean partial) implements StructuredObjectEntry<EntityTypePropertyType> {
    
}
