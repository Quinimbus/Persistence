package cloud.quinimbus.persistence.api.filter;

public interface PropertyFilter {

    public static enum Operator {
        EQUALS
    }

    String property();

    Operator operator();

    Object value();
}
