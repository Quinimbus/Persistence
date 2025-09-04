package cloud.quinimbus.persistence.api.schema;

import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.Optional;
import java.util.Set;

@RecordBuilder
@RecordBuilder.Options(
        useImmutableCollections = true,
        addSingleItemCollectionBuilders = true,
        addConcreteSettersForOptional = RecordBuilder.ConcreteSettersForOptionalMode.ENABLED)
public record EntityType(
        String id,
        Optional<String> idGenerator,
        Optional<OwningEntityTypeRef> owningEntity,
        Set<EntityTypeProperty> properties,
        Set<EntityTypeMigration> migrations)
        implements StructuredObjectType, EntityTypeBuilder.With {

    public static record OwningEntityTypeRef(String id, String field) {}

    @Override
    public Optional<EntityTypeProperty> property(String name) {
        return this.properties().stream().filter(etp -> etp.name().equals(name)).findAny();
    }
}
