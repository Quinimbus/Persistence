package cloud.quinimbus.persistence.api.schema;

import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.Map;

@RecordBuilder
@RecordBuilder.Options(useImmutableCollections = true, addSingleItemCollectionBuilders = true)
public record Schema(String id, Map<String, EntityType> entityTypes, Long version) implements SchemaBuilder.With {}
