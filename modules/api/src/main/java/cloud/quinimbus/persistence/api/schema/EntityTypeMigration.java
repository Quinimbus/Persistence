package cloud.quinimbus.persistence.api.schema;

import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
@RecordBuilder.Options(useImmutableCollections = true, addSingleItemCollectionBuilders = true)
public record EntityTypeMigration<T extends EntityTypeMigrationType>(String name, Long schemaVersion, T type)
        implements EntityTypeMigrationBuilder.With<T> {}
