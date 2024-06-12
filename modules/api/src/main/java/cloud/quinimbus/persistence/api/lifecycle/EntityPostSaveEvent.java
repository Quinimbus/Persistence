package cloud.quinimbus.persistence.api.lifecycle;

import cloud.quinimbus.persistence.api.entity.Entity;
import java.util.List;

public record EntityPostSaveEvent<K>(Entity<K> entity, List<String> mutatedProperties) implements LifecycleEvent<K> {
    
}
