package cloud.quinimbus.persistence.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

///
/// Marks an entity field to be mapped in a migration. The field value of each entity will be matched against each
/// mapping until one applies, the new value of that first matching mapping will be set.
///
/// @see cloud.quinimbus.persistence.api.schema.migrations.PropertyValueMappingMigrationType
/// @since 0.2
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldValueMappingMigration {

    /// The schema version this migration is part of
    long version();

    /// The mappings that should be applied
    Mapping[] value();

    /// Define the default behaviour if the old value is not present in the mappings
    MissingMappingOperation ifMissing() default MissingMappingOperation.KEEP;

    /// Describe a mapping for this field.
    ///
    /// @see cloud.quinimbus.persistence.api.schema.migrations.PropertyValueMappingMigrationType.Mapping
    public static @interface Mapping {

        /// The old value that should be mapped
        String oldValue();

        /// The new value that should be applied if the mapping matches
        String newValue();

        /// The operator to use to match this mapping
        Operator operator() default Operator.EQUALS;
    }

    /// An operator for matching a mapping.
    ///
    /// @see cloud.quinimbus.persistence.api.schema.migrations.PropertyValueMappingMigrationType.Operator
    public static enum Operator {
        /// Match if the string is exactly equals.
        EQUALS,
        /// Match if the string is equals ignoring the case.
        EQUALS_IGNORE_CASE,
        /// Use the old value of the mapping as regex to match.
        REGEX
    }

    /// The possible operations to do if no mapping matches the old value.
    ///
    /// @see cloud.quinimbus.persistence.api.schema.migrations.PropertyValueMappingMigrationType.MissingMappingOperation
    public static enum MissingMappingOperation {
        /// Keep the old value of the entity.
        KEEP,
        /// Set the property to null.
        SET_TO_NULL
    }
}
