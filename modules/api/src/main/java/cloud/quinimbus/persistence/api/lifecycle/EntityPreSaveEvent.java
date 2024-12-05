package cloud.quinimbus.persistence.api.lifecycle;

import cloud.quinimbus.persistence.api.entity.Entity;
import cloud.quinimbus.persistence.api.lifecycle.diff.Diff;
import java.util.Set;
import java.util.function.Consumer;

public record EntityPreSaveEvent<K>(Entity<K> entity, Set<Diff<Object>> diffs, Consumer<Entity<K>> changedEntity)
        implements LifecycleEvent<K>, EntityDiffEvent {}
