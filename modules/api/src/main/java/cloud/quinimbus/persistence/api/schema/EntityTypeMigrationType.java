package cloud.quinimbus.persistence.api.schema;

import cloud.quinimbus.persistence.api.schema.migrations.PropertyAddMigrationType;
import cloud.quinimbus.persistence.api.schema.migrations.PropertyValueMappingMigrationType;

public sealed interface EntityTypeMigrationType permits PropertyAddMigrationType, PropertyValueMappingMigrationType {}
