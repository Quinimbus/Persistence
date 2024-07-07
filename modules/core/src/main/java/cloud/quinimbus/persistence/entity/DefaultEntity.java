package cloud.quinimbus.persistence.entity;

import cloud.quinimbus.persistence.api.entity.Entity;
import cloud.quinimbus.persistence.api.entity.StructuredObjectEntry;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.EntityTypePropertyType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
public class DefaultEntity<K> extends AbstractDefaultStructuredObject<EntityTypePropertyType> implements Entity<K> {

    @Getter
    private final K id;

    @Getter
    private final EntityType type;

    public DefaultEntity(K id, EntityType type) {
        super(new LinkedHashMap<>());
        this.id = id;
        this.type = type;
    }

    public DefaultEntity(K id, EntityType type, Map<String, Object> properties) {
        super(new LinkedHashMap<>(properties));
        this.id = id;
        this.type = type;
    }

    public DefaultEntity(K id, EntityType type, Map<String, Object> properties, Map<String, Object> transientFields) {
        super(new LinkedHashMap<>(properties), new LinkedHashMap<>(transientFields));
        this.id = id;
        this.type = type;
    }

    public DefaultEntity(Entity<K> entity) {
        super(new LinkedHashMap<>(entity.getProperties()));
        this.id = entity.getId();
        this.type = entity.getType();
    }

    @Override
    public Optional<StructuredObjectEntry<EntityTypePropertyType>> getPropertyEntry(String id) {
        return this.type
                .property(id)
                .map(etp -> new DefaultEntityPropertyEntry(id, this.getProperty(id), etp.type(), false));
    }

    @Override
    public Optional<StructuredObjectEntry<EntityTypePropertyType>> getPropertyEntry(String id, Object partialValue) {
        var value = this.getProperty(id);
        return this.type.property(id).map(etp -> this.mapToPartialEntry(etp, value, partialValue));
    }
}
