package cloud.quinimbus.persistence.api.entity;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.lifecycle.LifecycleEvent;
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

    public abstract void init();
}
