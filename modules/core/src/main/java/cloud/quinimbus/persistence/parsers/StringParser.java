package cloud.quinimbus.persistence.parsers;

public final class StringParser implements ValueParser<String> {

    @Override
    public String parse(Object o) {
        return o == null ? null : o.toString();
    }
}
