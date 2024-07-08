package cloud.quinimbus.persistence.api.lifecycle;

import cloud.quinimbus.persistence.api.entity.Entity;
import java.util.Set;
import java.util.function.Consumer;

public record EntityPreSaveEvent<K>(Entity<K> entity, Set<String> mutatedProperties, Consumer<Entity<K>> changedEntity)
        implements LifecycleEvent<K> {}
