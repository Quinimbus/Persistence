package cloud.quinimbus.persistence.api.lifecycle;

import cloud.quinimbus.persistence.api.entity.Entity;
import java.util.Set;

public record EntityPostSaveEvent<K>(Entity<K> entity, Set<String> mutatedProperties) implements LifecycleEvent<K> {}
