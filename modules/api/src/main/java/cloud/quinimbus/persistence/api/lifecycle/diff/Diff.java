package cloud.quinimbus.persistence.api.lifecycle.diff;

public sealed interface Diff<T>
        permits CompletePropertyDiff,
                ListPropertyEntryAddedDiff,
                ListPropertyEntryRemovedDiff,
                MapPropertyEntryAddedDiff,
                MapPropertyEntryRemovedDiff,
                MapPropertyEntryReplacedDiff,
                SetPropertyEntryAddedDiff,
                SetPropertyEntryRemovedDiff {

    String name();

    T oldValue();

    T newValue();
}
