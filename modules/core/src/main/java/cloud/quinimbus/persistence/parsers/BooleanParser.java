package cloud.quinimbus.persistence.parsers;

import cloud.quinimbus.persistence.api.entity.UnparseableValueException;

public final class BooleanParser implements ValueParser<Boolean> {

    @Override
    public Boolean parse(Object o) throws UnparseableValueException {
        if (o == null) {
            return null;
        } else if (o instanceof Boolean b) {
            return b;
        } else if (o instanceof String s) {
            return Boolean.valueOf(s);
        } else {
            throw new UnparseableValueException(
                    "Cannot read value of type %s as Boolean".formatted(o.getClass().getName()));
        }
    }
}
