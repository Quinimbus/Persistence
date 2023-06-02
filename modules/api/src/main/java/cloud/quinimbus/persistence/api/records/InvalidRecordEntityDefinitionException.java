package cloud.quinimbus.persistence.api.records;

import cloud.quinimbus.persistence.api.schema.InvalidSchemaException;

public class InvalidRecordEntityDefinitionException extends InvalidSchemaException {
    
    private Class<? extends Record> recordType;
    
    public InvalidRecordEntityDefinitionException(Class<? extends Record> recordType, String message) {
        super("Invalid record entity definition %s: %s".formatted(recordType.getName(), message));
    }

    public Class<? extends Record> recordType() {
        return recordType;
    }
}
