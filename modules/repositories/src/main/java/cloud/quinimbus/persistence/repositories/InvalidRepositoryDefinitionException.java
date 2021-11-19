package cloud.quinimbus.persistence.repositories;

public class InvalidRepositoryDefinitionException extends Exception {

    public InvalidRepositoryDefinitionException(String message) {
        super(message);
    }

    public InvalidRepositoryDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
