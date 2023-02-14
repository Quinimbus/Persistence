package cloud.quinimbus.persistence.api.schema;

import java.time.Instant;
import java.util.Set;
import lombok.Builder;
import lombok.With;

@Builder
@With
public record Metadata(String id, Long version, Instant creationTime, Set<MigrationRun> entityTypeMigrationRuns) {

    public static record MigrationRun(String identifier, String entityType, Long schemaVersion, Instant runAt) {
    }
}
