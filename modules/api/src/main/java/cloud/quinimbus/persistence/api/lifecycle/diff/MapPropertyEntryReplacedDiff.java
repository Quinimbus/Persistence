package cloud.quinimbus.persistence.api.lifecycle.diff;

public record MapPropertyEntryReplacedDiff<T>(String name, String key, T oldValue, T newValue) implements Diff<T> {}
