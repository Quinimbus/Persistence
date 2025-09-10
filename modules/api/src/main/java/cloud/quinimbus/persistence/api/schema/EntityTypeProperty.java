package cloud.quinimbus.persistence.api.schema;

import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
@RecordBuilder.Options(useImmutableCollections = true, addSingleItemCollectionBuilders = true)
public record EntityTypeProperty<T extends EntityTypePropertyType>(
        String name, T type, @RecordBuilder.Initializer("DEFAULT_STRUCTURE") Structure structure)
        implements EntityTypePropertyBuilder.With<T> {

    public static final Structure DEFAULT_STRUCTURE = Structure.SINGLE;

    public static enum Structure {
        SINGLE,
        LIST,
        SET,
        MAP
    }
}
