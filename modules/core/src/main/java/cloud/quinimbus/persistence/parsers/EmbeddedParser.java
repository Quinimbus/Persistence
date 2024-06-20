package cloud.quinimbus.persistence.parsers;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.entity.EmbeddedObject;
import cloud.quinimbus.persistence.api.entity.UnparseableValueException;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.properties.EmbeddedPropertyType;
import java.util.List;
import java.util.Map;

public final class EmbeddedParser implements ValueParser<EmbeddedObject> {

    private final List<String> path;

    private final EntityType parentType;

    private final EmbeddedPropertyType embeddedPropertyType;

    private final PersistenceContext persistenceContext;

    public EmbeddedParser(
            EmbeddedPropertyType embeddedPropertyType,
            List<String> path,
            EntityType parentType,
            PersistenceContext persistenceContext) {
        this.embeddedPropertyType = embeddedPropertyType;
        this.path = path;
        this.parentType = parentType;
        this.persistenceContext = persistenceContext;
    }

    @Override
    public EmbeddedObject parse(Object o) throws UnparseableValueException {
        if (o == null) {
            return null;
        } else if (o instanceof Map m) {
            return this.persistenceContext.newEmbedded(this.embeddedPropertyType, this.parentType, this.path, m);
        } else if (o instanceof EmbeddedObject eo) {
            return eo;
        }
        throw new UnparseableValueException("Cannot read value of type %s as Embedded"
                .formatted(o.getClass().getName()));
    }
}
