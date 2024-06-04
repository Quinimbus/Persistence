package cloud.quinimbus.persistence.api.lifecycle;

import cloud.quinimbus.persistence.api.entity.Entity;

public record EntityPostSaveEvent<K>(Entity<K> entity) implements LifecycleEvent<K> {
    
}
