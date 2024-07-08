package cloud.quinimbus.persistence.api.entity;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.lifecycle.LifecycleEvent;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Getter;

public abstract class EmbeddedPropertyHandler {

    @Getter
    private final PersistenceContext context;

    @Getter
    private final String schemaId;

    @Getter
    private final String typeId;

    @Getter
    private final String property;

    public EmbeddedPropertyHandler(PersistenceContext context, String schemaId, String typeId, String property) {
        this.context = context;
        this.schemaId = schemaId;
        this.typeId = typeId;
        this.property = property;
    }

    public <T extends LifecycleEvent> void onLifecycleEvent(Class<T> eventType, Consumer<T> consumer) {
        this.context.onLifecycleEvent(schemaId, eventType, typeId, consumer);
    }

    public EmbeddedObject newEmbedded(Map<String, Object> properties, Map<String, Object> transientFields)
            throws UnparseableValueException {
        var schema = context.getSchema(this.schemaId)
                .orElseThrow(() -> new IllegalStateException("Schema % not found.".formatted(this.schemaId)));
        var parentType = schema.entityTypes().get(this.typeId);
        var type = parentType
                .embeddedPropertyType(this.property)
                .orElseThrow(() ->
                        new IllegalStateException("Embedded type for property %s not found on type %s in schema %s."
                                .formatted(this.property, this.typeId, this.schemaId)));
        return this.context.newEmbedded(type, parentType, List.of(this.property), properties, transientFields);
    }

    public abstract void init();
}
