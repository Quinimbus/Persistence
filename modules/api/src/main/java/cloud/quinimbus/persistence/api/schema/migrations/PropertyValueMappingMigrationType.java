package cloud.quinimbus.persistence.api.schema.migrations;

import cloud.quinimbus.persistence.api.schema.EntityTypeMigrationType;
import java.util.List;

///
/// Mark a migration as mapping migration. The field value of each entity will be matched against each mapping until one
/// applies, the new value of that first matching mapping will be set.
///
/// @param field                    The field this migration applies to
/// @param mappings                 The mappings to apply
/// @param missingMappingOperation  The operation to apply if no mapping matches the old value
/// @since 0.2
public record PropertyValueMappingMigrationType(
        String field, List<Mapping> mappings, MissingMappingOperation missingMappingOperation)
        implements EntityTypeMigrationType {

    /// A mapping to apply to a value of the field
    ///
    /// @param oldValue  The old value that should be mapped
    /// @param newValue  The new value that should be applied if the mapping matches
    /// @param operator  The operator to use to match this mapping
    public static record Mapping(String oldValue, String newValue, Operator operator) {}

    /// An operator for matching a mapping.
    public static enum Operator {
        /// Match if the string is exactly equals.
        EQUALS,
        /// Match if the string is equals ignoring the case.
        EQUALS_IGNORE_CASE,
        /// Use the old value of the mapping as regex to match.
        REGEX
    }

    /// The possible operations to do if no mapping matches the old value.
    public static enum MissingMappingOperation {
        /// Keep the old value of the entity.
        KEEP,
        /// Set the property to null.
        SET_TO_NULL
    }
}
