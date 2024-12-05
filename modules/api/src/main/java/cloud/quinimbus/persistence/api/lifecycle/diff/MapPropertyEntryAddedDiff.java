package cloud.quinimbus.persistence.api.lifecycle.diff;

public record MapPropertyEntryAddedDiff<T>(String name, String key, T newValue) implements Diff<T> {

    @Override
    public T oldValue() {
        return null;
    }
}
