package cloud.quinimbus.persistence.api.entity;

import cloud.quinimbus.persistence.api.PersistenceException;

public class UnparseableValueException extends PersistenceException {

    public UnparseableValueException(String message) {
        super(message);
    }
}
