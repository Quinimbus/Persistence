package cloud.quinimbus.persistence.parsers;

import cloud.quinimbus.persistence.api.entity.UnparseableValueException;
import java.time.LocalDate;

public final class LocalDateParser implements ValueParser<LocalDate> {
    
    @Override
    public LocalDate parse(Object o) throws UnparseableValueException {
        if (o == null) {
            return null;
        } else if (o instanceof LocalDate ld) {
            return ld;
        } else if (o instanceof String s) {
            return LocalDate.parse(s);
        } else {
            throw new UnparseableValueException(
                    "Cannot read value of type %s as LocalDate".formatted(o.getClass().getName()));
        }
    }
    
}
