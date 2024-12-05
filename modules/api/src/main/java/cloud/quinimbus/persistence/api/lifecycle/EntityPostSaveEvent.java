package cloud.quinimbus.persistence.api.lifecycle;

import cloud.quinimbus.persistence.api.entity.Entity;
import cloud.quinimbus.persistence.api.lifecycle.diff.Diff;
import java.util.Set;

public record EntityPostSaveEvent<K>(Entity<K> entity, Set<Diff<Object>> diffs)
        implements LifecycleEvent<K>, EntityDiffEvent {}
