package cloud.quinimbus.persistence.api.lifecycle.diff;

public record SetPropertyEntryAddedDiff<T>(String name, T newValue) implements Diff<T> {

    @Override
    public T oldValue() {
        return null;
    }
}
