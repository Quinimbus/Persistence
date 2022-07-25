package cloud.quinimbus.persistence.entity;

import cloud.quinimbus.persistence.api.entity.StructuredObject;
import cloud.quinimbus.persistence.api.entity.StructuredObjectEntry;
import cloud.quinimbus.persistence.api.entity.StructuredObjectEntryType;
import cloud.quinimbus.persistence.api.schema.EntityTypeProperty;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
abstract class AbstractDefaultStructuredObject<ET extends StructuredObjectEntryType> implements StructuredObject<ET> {

    protected final Map<String, Object> properties;

    public AbstractDefaultStructuredObject(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public <PT> PT getProperty(String id) {
        return (PT) this.properties.get(id);
    }

    @Override
    public <PT> void setProperty(String id, PT value) {
        this.properties.put(id, value);
    }

    @Override
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(this.properties);
    }

    @Override
    public Map<String, Object> asBasicMap() {
        return this.properties.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> valueForMap(e.getValue())));
    }

    @Override
    public Map<String, Object> asBasicMap(Function<StructuredObjectEntry<ET>, Object> converter) {
        return this.properties.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), valueForMap(e.getKey(), e.getValue(), converter)))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue));
    }

    private Object valueForMap(Object o) {
        if (o instanceof StructuredObject so) {
            return so.asBasicMap();
        } else if (o instanceof List l) {
            return l.stream().map(this::valueForMap).toList();
        } else if (o instanceof Set s) {
            return s.stream().map(this::valueForMap).collect(Collectors.toSet());
        } else {
            return o;
        }
    }

    private Object valueForMap(String key, Object o, Function<StructuredObjectEntry<ET>, Object> converter) {
        if (o instanceof StructuredObject so) {
            return so.asBasicMap(converter);
        } else if (o instanceof List l) {
            return l.stream().map(e -> valueForMap(key, e, converter)).toList();
        } else if (o instanceof Set s) {
            return s.stream().map(e -> valueForMap(key, e, converter)).collect(Collectors.toSet());
        } else {
            return converter.apply(this.getPropertyEntry(key, o)
                    .orElseThrow(() -> new IllegalStateException("Missing property entry for " + key)));
        }
    }

    protected DefaultEntityPropertyEntry mapToPartialEntry(EntityTypeProperty etp, Object value, Object partialValue) {
        return switch (etp.structure()) {
            case SINGLE ->
                Objects.equals(partialValue, value)
                ? new DefaultEntityPropertyEntry(
                etp.name(),
                value,
                etp.type(),
                false) : null;
            case LIST ->
                (value != null && value instanceof List l && l.contains(partialValue))
                ? new DefaultEntityPropertyEntry(
                etp.name(),
                partialValue,
                etp.type(),
                true) : null;
            case SET ->
                (value != null && value instanceof Set s && s.contains(partialValue))
                ? new DefaultEntityPropertyEntry(
                etp.name(),
                partialValue,
                etp.type(),
                true) : null;
            default ->
                throw new IllegalStateException();
        };
    }
}
