package cloud.quinimbus.persistence.api.lifecycle.diff;

public record SetPropertyEntryRemovedDiff<T>(String name, T oldValue) implements Diff<T> {

    @Override
    public T newValue() {
        return null;
    }
}
