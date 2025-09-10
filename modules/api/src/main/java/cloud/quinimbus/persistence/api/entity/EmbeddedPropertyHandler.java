package cloud.quinimbus.persistence.api.entity;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.lifecycle.LifecycleEvent;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.EntityTypeProperty;
import cloud.quinimbus.persistence.api.schema.Schema;
import cloud.quinimbus.persistence.api.schema.properties.EmbeddedPropertyType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.Getter;

/// This class is the base class to implement embeddable property handlers. Property handlers can be used to implement
/// custom handling of embeddable properties. If you use records for the domain model you have to register such a
/// handler using the [cloud.quinimbus.persistence.api.annotation.Embeddable#handler()] field at the embeddable record.
/// For the json variant you have to add the field `handlerClass`
public abstract class EmbeddedPropertyHandler {

    @Getter
    private final PersistenceContext context;

    @Getter
    private final String schemaId;

    @Getter
    private final String typeId;

    @Getter
    private final String property;

    private Schema schema;

    private EntityType parentType;

    private EmbeddedPropertyType propertyType;

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
        return this.context.newEmbedded(
                getPropertyType(), getParentType(), List.of(this.property), properties, transientFields);
    }

    /// @return The schema associated to the entity this handler is registered to
    public Schema getSchema() {
        return this.schema != null
                ? this.schema
                : context.getSchema(this.schemaId)
                        .orElseThrow(() -> new IllegalStateException("Schema % not found.".formatted(this.schemaId)));
    }

    /// @return The parent entity type of the entity this embeddable is embedded into
    public EntityType getParentType() {
        return this.parentType != null
                ? this.parentType
                : getSchema().entityTypes().get(this.typeId);
    }

    /// @return The embedded property type of the entity this embeddable is embedded into
    public EmbeddedPropertyType getPropertyType() {
        return this.propertyType != null
                ? this.propertyType
                : getParentType()
                        .embeddedPropertyType(this.property)
                        .orElseThrow(() -> new IllegalStateException(
                                "Embedded type for property %s not found on type %s in schema %s."
                                        .formatted(this.property, this.typeId, this.schemaId)));
    }

    /// @return The property of the entity this embeddable is embedded into
    public EntityTypeProperty getEntityTypeProperty() {
        return this.getParentType()
                .property(this.property)
                .orElseThrow(() -> new IllegalStateException("Property %s not found on type %s in schema %s."
                        .formatted(this.property, this.typeId, this.schemaId)));
    }

    ///
    /// @param name The name of the requested context
    /// @param type The type of the requested context
    /// @param <T>  The type of the property context
    /// @return The requested property context or an empty [Optional] if no context is available for the given name
    public <T extends PropertyContext> Optional<T> getPropertyContext(String name, Class<T> type) {
        var ctx = this.getEntityTypeProperty().context().get(name);
        if (ctx == null) {
            return Optional.empty();
        }
        if (type.isAssignableFrom(ctx.getClass())) {
            return Optional.of((T) ctx);
        }
        throw new IllegalArgumentException("There is a context named %s, but it is of type %s and not of typpe %s"
                .formatted(name, ctx.getClass().getSimpleName(), type.getSimpleName()));
    }

    public abstract void init();
}
