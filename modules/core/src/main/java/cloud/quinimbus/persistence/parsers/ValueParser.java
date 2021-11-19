package cloud.quinimbus.persistence.parsers;

import cloud.quinimbus.persistence.api.entity.UnparseableValueException;

public sealed interface ValueParser<T> permits BooleanParser, EmbeddedParser, EnumParser, IntegerParser, StringParser, TimestampParser {

    T parse(Object o) throws UnparseableValueException;
}
