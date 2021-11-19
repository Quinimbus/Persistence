package cloud.quinimbus.persistence.entity;

import cloud.quinimbus.persistence.api.entity.EmbeddedObject;
import cloud.quinimbus.persistence.api.schema.EntityType;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString(exclude = "parentType")
@EqualsAndHashCode(exclude = "parentType", callSuper = true)
public class DefaultEmbeddedObject extends AbstractDefaultStructuredObject implements EmbeddedObject {

    @Getter
    private final String[] path;

    @Getter
    private final EntityType parentType;

    public DefaultEmbeddedObject(String[] path, EntityType parentType, Map<String, Object> properties) {
        super(properties);
        this.path = path;
        this.parentType = parentType;
    }
}
