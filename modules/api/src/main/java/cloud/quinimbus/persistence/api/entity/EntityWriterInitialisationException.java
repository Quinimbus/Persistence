package cloud.quinimbus.persistence.api.entity;

public class EntityWriterInitialisationException extends Exception {

    public EntityWriterInitialisationException(String message) {
        super(message);
    }

    public EntityWriterInitialisationException(String message, Throwable cause) {
        super(message, cause);
    }
}
