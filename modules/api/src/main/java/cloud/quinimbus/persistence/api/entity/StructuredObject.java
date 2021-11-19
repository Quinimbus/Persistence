package cloud.quinimbus.persistence.api.entity;

import java.util.Map;

public interface StructuredObject {

    <PT> PT getProperty(String id);

    <PT> void setProperty(String id, PT value);

    Map<String, Object> getProperties();

    Map<String, Object> asBasicMap();
}
