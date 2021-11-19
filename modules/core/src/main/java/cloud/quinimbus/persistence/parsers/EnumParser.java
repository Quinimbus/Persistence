package cloud.quinimbus.persistence.parsers;

import cloud.quinimbus.persistence.api.entity.UnparseableValueException;
import java.util.List;
import java.util.stream.Collectors;

public final class EnumParser implements ValueParser<String> {

    private final List<String> allowedValues;

    public EnumParser(List<String> allowedValues) {
        this.allowedValues = allowedValues;
    }

    @Override
    public String parse(Object o) throws UnparseableValueException {
        if (o == null) {
            return null;
        } else if (o instanceof String s) {
            if (this.allowedValues.contains(s)) {
                return s;
            } else {
                throw new UnparseableValueException(
                        "Value %s is not allowed for enum[%s]".formatted(
                                s,
                                this.allowedValues.stream().collect(Collectors.joining(", "))));
            }
        } else {
            throw new UnparseableValueException(
                    "Cannot read value of type %s as Enum".formatted(o.getClass().getName()));
        }
    }
}
