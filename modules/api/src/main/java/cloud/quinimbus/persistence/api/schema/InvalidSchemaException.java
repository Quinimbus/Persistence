package cloud.quinimbus.persistence.api.schema;

public class InvalidSchemaException extends Exception {

    public InvalidSchemaException(String message) {
        super(message);
    }

    public InvalidSchemaException(String message, Throwable cause) {
        super(message, cause);
    }
}
