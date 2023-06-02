package cloud.quinimbus.persistence.records;

import cloud.quinimbus.common.tools.Records;
import cloud.quinimbus.persistence.api.annotation.EntityIdField;
import cloud.quinimbus.persistence.api.records.InvalidRecordEntityDefinitionException;
import cloud.quinimbus.persistence.api.records.RecordEntityRegistry;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class RecordEntityRegistryImpl implements RecordEntityRegistry {
    
    private final Map<Class<? extends Record>, String> idFields;

    public RecordEntityRegistryImpl() {
        this.idFields = new LinkedHashMap<>();
    }
    
    public void register(Class<? extends Record> recordClass) throws InvalidRecordEntityDefinitionException {
        if (this.idFields.containsKey(recordClass)) {
            throw new IllegalArgumentException("%s is already registered".formatted(recordClass.getName()));
        }
        this.idFields.put(recordClass, this.findIdField(recordClass));
    }
    
    private <T extends Record> String findIdField(Class<T> recordClass) throws InvalidRecordEntityDefinitionException {
        var possibleIdFields = Arrays.stream(recordClass.getDeclaredFields())
                .filter(f -> f.getAnnotation(EntityIdField.class) != null)
                .toList();
        if (possibleIdFields.isEmpty()) {
            throw new InvalidRecordEntityDefinitionException(
                    recordClass,
                    "Cannot automatically detect any id field and no id field was given");
        } else if (possibleIdFields.size() > 1) {
            throw new InvalidRecordEntityDefinitionException(
                    recordClass,
                    "Multiple possible id fields found and no id field was given");
        }
        return possibleIdFields.get(0).getName();
    }
    
    public <T extends Record> String getIdField(Class<T> recordClass) {
        var idField = this.idFields.get(recordClass);
        if (idField == null) {
            throw new IllegalStateException("Unknown record class: %s".formatted(recordClass.getName()));
        }
        return idField;
    }

    public <T extends Record, K> Function<T, K> getIdValueGetter(Class<T> entityClass) {
        return Records.fieldValueGetter(entityClass, this.getIdField(entityClass));
    }
}
