package cloud.quinimbus.persistence.api.entity;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public interface StructuredObject<ET extends StructuredObjectEntryType> {

    <PT> PT getProperty(String id);

    Optional<StructuredObjectEntry<ET>> getPropertyEntry(String id);

    Optional<StructuredObjectEntry<ET>> getPropertyEntry(String id, Object partialValue);

    <PT> void setProperty(String id, PT value);

    Map<String, Object> getProperties();

    Map<String, Object> asBasicMap();

    Map<String, Object> asBasicMap(Function<StructuredObjectEntry<ET>, Object> converter);

    Map<String, Object> getTransientFields();

    void clearTransientFields();

    boolean hasProperty(String id);
}
