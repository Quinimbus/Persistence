package cloud.quinimbus.persistence.api.schema;

import cloud.quinimbus.persistence.api.entity.PropertyContext;
import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.Map;

@RecordBuilder
@RecordBuilder.Options(useImmutableCollections = true, addSingleItemCollectionBuilders = true)
public record EntityTypeProperty<T extends EntityTypePropertyType>(
        String name,
        T type,
        @RecordBuilder.Initializer("DEFAULT_STRUCTURE") Structure structure,
        @RecordBuilder.Initializer("DEFAULT_CONTEXT") Map<String, ? extends PropertyContext> context)
        implements EntityTypePropertyBuilder.With<T> {

    public static final Structure DEFAULT_STRUCTURE = Structure.SINGLE;
    public static final Map<String, ? extends PropertyContext> DEFAULT_CONTEXT = Map.of();

    public static enum Structure {
        SINGLE,
        LIST,
        SET,
        MAP
    }
}
