package cloud.quinimbus.persistence.api.schema.migrations;

import cloud.quinimbus.persistence.api.schema.EntityTypeMigrationType;
import java.util.Map;

public record PropertyAddMigrationType(Map<String, Object> properties) implements EntityTypeMigrationType{
    
}
