package cloud.quinimbus.persistence.parsers;

import cloud.quinimbus.persistence.api.entity.UnparseableValueException;
import java.time.Instant;
import java.util.Date;

public final class TimestampParser implements ValueParser<Instant> {

    @Override
    public Instant parse(Object o) throws UnparseableValueException {
        if (o == null) {
            return null;
        } else if (o instanceof Instant i) {
            return i;
        } else if (o instanceof String s) {
            return Instant.parse(s);
        } else if (o instanceof Date d) {
            return d.toInstant();
        } else {
            throw new UnparseableValueException("Cannot read value of type %s as Timestamp"
                    .formatted(o.getClass().getName()));
        }
    }
}
