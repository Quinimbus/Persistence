package cloud.quinimbus.persistence.api.schema;

import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
@RecordBuilder.Options(useImmutableCollections = true, addSingleItemCollectionBuilders = true)
public record EntityTypeProperty<T extends EntityTypePropertyType>(String name, T type, Structure structure)
        implements EntityTypePropertyBuilder.With<T> {

    public static enum Structure {
        SINGLE,
        LIST,
        SET,
        MAP
    }
}
