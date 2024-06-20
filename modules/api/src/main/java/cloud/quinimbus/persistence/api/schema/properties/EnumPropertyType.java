package cloud.quinimbus.persistence.api.schema.properties;

import cloud.quinimbus.persistence.api.schema.EntityTypePropertyType;
import java.util.List;

public final record EnumPropertyType(List<String> allowedValues) implements EntityTypePropertyType {}
