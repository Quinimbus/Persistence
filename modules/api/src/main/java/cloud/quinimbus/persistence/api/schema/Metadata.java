package cloud.quinimbus.persistence.api.schema;

import io.soabase.recordbuilder.core.RecordBuilder;
import java.time.Instant;
import java.util.Set;

@RecordBuilder
@RecordBuilder.Options(useImmutableCollections = true, addSingleItemCollectionBuilders = true)
public record Metadata(String id, Long version, Instant creationTime, Set<MigrationRun> entityTypeMigrationRuns)
        implements MetadataBuilder.With {

    public static record MigrationRun(String identifier, String entityType, Long schemaVersion, Instant runAt) {}
}
