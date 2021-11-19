package cloud.quinimbus.persistence.entity;

import cloud.quinimbus.persistence.api.entity.Entity;
import cloud.quinimbus.persistence.api.schema.EntityType;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
public class DefaultEntity<K> extends AbstractDefaultStructuredObject implements Entity<K> {

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

    public DefaultEntity(Entity<K> entity) {
        super(new LinkedHashMap<>(entity.getProperties()));
        this.id = entity.getId();
        this.type = entity.getType();
    }
}
