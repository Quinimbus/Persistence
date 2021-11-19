package cloud.quinimbus.persistence.entity;

import cloud.quinimbus.persistence.api.entity.StructuredObject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class AbstractDefaultStructuredObject implements StructuredObject {

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

    public Map<String, Object> asBasicMap() {
        return this.properties.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> valueForMap(e.getValue())));
    }

    private static Object valueForMap(Object o) {
        if (o instanceof StructuredObject so) {
            return so.asBasicMap();
        } else if (o instanceof List l) {
            return l.stream().map(AbstractDefaultStructuredObject::valueForMap).toList();
        } else if (o instanceof Set s) {
            return s.stream().map(AbstractDefaultStructuredObject::valueForMap).collect(Collectors.toSet());
        } else {
            return o;
        }
    }
}
