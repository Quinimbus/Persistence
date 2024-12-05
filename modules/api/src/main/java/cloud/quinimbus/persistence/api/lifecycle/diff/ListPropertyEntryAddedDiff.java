package cloud.quinimbus.persistence.api.lifecycle.diff;

public record ListPropertyEntryAddedDiff<T>(String name, int index, T newValue) implements Diff<T> {

    @Override
    public T oldValue() {
        return null;
    }
}
