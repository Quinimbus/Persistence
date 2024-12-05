package cloud.quinimbus.persistence.api.lifecycle.diff;

public record CompletePropertyDiff<T>(String name, T oldValue, T newValue) implements Diff<T> {}
