package cloud.quinimbus.persistence.parsers;

import cloud.quinimbus.persistence.api.entity.UnparseableValueException;

public final class IntegerParser implements ValueParser<Number> {

    @Override
    public Number parse(Object o) throws UnparseableValueException {
        if (o == null) {
            return null;
        } else if (o instanceof String s) {
            return Long.parseLong(s);
        } else if (o instanceof Byte b) {
            return b;
        } else if (o instanceof Short s) {
            return s;
        } else if (o instanceof Integer i) {
            return i;
        } else if (o instanceof Long l) {
            return l;
        } else {
            throw new UnparseableValueException("Cannot read value %s of type %s as Integer"
                    .formatted(o.toString(), o.getClass().getName()));
        }
    }
}
