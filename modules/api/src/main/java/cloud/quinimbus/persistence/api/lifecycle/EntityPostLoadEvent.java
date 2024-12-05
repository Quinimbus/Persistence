package cloud.quinimbus.persistence.api.lifecycle;

import cloud.quinimbus.persistence.api.entity.Entity;
import java.util.function.Consumer;

public record EntityPostLoadEvent<K>(Entity<K> entity, Consumer<Entity<K>> changedEntity)
        implements LifecycleEvent<K> {}
