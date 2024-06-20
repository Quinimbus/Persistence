package cloud.quinimbus.persistence.api.schema;

import cloud.quinimbus.persistence.api.schema.migrations.PropertyAddMigrationType;

public sealed interface EntityTypeMigrationType permits PropertyAddMigrationType {}
