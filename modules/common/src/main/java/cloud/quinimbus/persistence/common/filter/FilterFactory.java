package cloud.quinimbus.persistence.common.filter;

import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.filter.PropertyFilter;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import name.falgout.jeffrey.throwing.stream.ThrowingStream;

public class FilterFactory {
    
    public static record DefaultPropertyFilter(String property, PropertyFilter.Operator operator, Object value) implements PropertyFilter {
        
    }
    
    public static Set<? extends PropertyFilter> fromMap(Map<String, Object> propertyFilters) {
        return propertyFilters.entrySet().stream()
                .map(e -> new DefaultPropertyFilter(e.getKey(), PropertyFilter.Operator.EQUALS, e.getValue()))
                .collect(Collectors.toSet());
    }
    
    public static <R extends Record> Set<? extends PropertyFilter> fromRecord(R record) throws PersistenceException {
        try {
            return ThrowingStream.of(Arrays.stream(record.getClass().getRecordComponents()), ReflectiveOperationException.class)
                    .map(rc -> new DefaultPropertyFilter(rc.getName(), PropertyFilter.Operator.EQUALS, rc.getAccessor().invoke(record)))
                    .collect(Collectors.toSet());
        } catch (ReflectiveOperationException ex) {
            throw new PersistenceException("Failed to analyze the filter record type " + record.getClass().getName(), ex);
        }
    }
}
