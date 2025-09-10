package cloud.quinimbus.persistence.api.records;

import cloud.quinimbus.persistence.api.entity.PropertyContext;
import java.lang.reflect.Field;
import java.util.Optional;

/// This is the base interface for a property context handler for record entities. An implementation of this interface
/// has to be registered as described in the [java.util.ServiceLoader] and has to have a
/// [cloud.quinimbus.common.annotations.Provider] annotation providing the name.
/// @since 0.2
public interface RecordPropertyContextHandler {

    /// This method should be implemented to analyze the field and create a [PropertyContext] if required.
    /// @param field The field that is currently processed
    /// @return A constructed [PropertyContext] or an empty [Optional]
    Optional<? extends PropertyContext> createContext(Field field);
}
