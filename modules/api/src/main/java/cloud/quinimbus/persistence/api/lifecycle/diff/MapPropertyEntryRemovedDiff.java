package cloud.quinimbus.persistence.api.lifecycle.diff;

public record MapPropertyEntryRemovedDiff<T>(String name, String key, T oldValue) implements Diff<T> {

    @Override
    public T newValue() {
        return null;
    }
}
