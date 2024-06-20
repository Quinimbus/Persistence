package cloud.quinimbus.persistence.entity;

import static cloud.quinimbus.tools.stream.QCollectors.*;

import cloud.quinimbus.persistence.api.entity.EmbeddedObject;
import cloud.quinimbus.persistence.api.entity.StructuredObjectEntry;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.EntityTypeProperty;
import cloud.quinimbus.persistence.api.schema.EntityTypePropertyType;
import cloud.quinimbus.persistence.api.schema.properties.EmbeddedPropertyType;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString(exclude = "parentType")
@EqualsAndHashCode(
        exclude = {"parentType", "propertyType", "embeddedPropertyTypes"},
        callSuper = true)
public class DefaultEmbeddedObject extends AbstractDefaultStructuredObject<EntityTypePropertyType>
        implements EmbeddedObject {

    @Getter
    private final String[] path;

    @Getter
    private final EntityType parentType;

    private final EmbeddedPropertyType propertyType;

    private final Map<String, EntityTypeProperty> embeddedPropertyTypes;

    public DefaultEmbeddedObject(
            String[] path, EntityType parentType, Map<String, Object> properties, EmbeddedPropertyType propertyType) {
        super(properties);
        this.path = path;
        this.parentType = parentType;
        this.propertyType = propertyType;
        this.embeddedPropertyTypes = propertyType.properties().stream()
                .collect(Collectors.groupingBy(EntityTypeProperty::name, exactlyOne()));
    }

    @Override
    public Optional<StructuredObjectEntry<EntityTypePropertyType>> getPropertyEntry(String id) {
        return Optional.ofNullable(this.embeddedPropertyTypes.get(id))
                .map(etp -> new DefaultEntityPropertyEntry(id, this.getProperty(id), etp.type(), false));
    }

    @Override
    public Optional<StructuredObjectEntry<EntityTypePropertyType>> getPropertyEntry(String id, Object partialValue) {
        var value = this.getProperty(id);
        return Optional.ofNullable(this.embeddedPropertyTypes.get(id))
                .map(etp -> this.mapToPartialEntry(etp, value, partialValue));
    }
}
