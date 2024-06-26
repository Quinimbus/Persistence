package cloud.quinimbus.persistence.lifecycle;

import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.entity.Entity;
import cloud.quinimbus.persistence.api.lifecycle.EntityPostSaveEvent;
import cloud.quinimbus.persistence.api.lifecycle.LifecycleEvent;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorage;
import cloud.quinimbus.persistence.common.storage.PersistenceSchemaStorageDelegate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class LifecyclePersistenceSchemaStorageDelegate extends PersistenceSchemaStorageDelegate {

    private final Map<Class<? extends LifecycleEvent>, Map<String, List<Consumer<? extends LifecycleEvent>>>> consumers;

    public LifecyclePersistenceSchemaStorageDelegate(PersistenceSchemaStorage delegate) {
        super(delegate);
        this.consumers = new HashMap<>();
    }

    public <T extends LifecycleEvent> void addConsumer(Class<T> eventType, EntityType type, Consumer<T> consumer) {
        this.addConsumer(eventType, type.id(), consumer);
    }

    public <T extends LifecycleEvent> void addConsumer(Class<T> eventType, String typeId, Consumer<T> consumer) {
        var consumersList = this.consumers
                .computeIfAbsent(eventType, t -> new HashMap<>())
                .computeIfAbsent(typeId, t -> new ArrayList<>());
        consumersList.add(consumer);
    }

    @Override
    public <K> void save(Entity<K> entity) throws PersistenceException {
        var mutatedProperties = new ArrayList<String>();
        super.find(entity.getType(), entity.getId())
                .ifPresentOrElse(
                        oldEntity -> {
                            mutatedProperties.addAll(compareEntities(entity, oldEntity));
                        },
                        () -> {
                            mutatedProperties.addAll(entity.getProperties().keySet());
                        });
        super.save(entity);
        var postSaveEvent = new EntityPostSaveEvent<K>(entity, mutatedProperties);
        Optional.ofNullable(this.consumers.get(EntityPostSaveEvent.class))
                .map(m -> m.get(entity.getType().id()))
                .ifPresent(cl -> {
                    cl.stream().map(c -> (Consumer<EntityPostSaveEvent<K>>) c).forEach(c -> c.accept(postSaveEvent));
                });
    }

    private <K> List<String> compareEntities(Entity<K> newEntity, Entity<K> oldEntity) {
        var allPropertyKeys = new HashSet<String>();
        allPropertyKeys.addAll(newEntity.getProperties().keySet());
        allPropertyKeys.addAll(oldEntity.getProperties().keySet());
        return allPropertyKeys.stream()
                .filter(key -> {
                    var oldValue = oldEntity.getProperty(key);
                    var newValue = newEntity.getProperty(key);
                    if ((oldValue == null && newValue != null) || (oldValue != null && newValue == null)) {
                        return true;
                    } else if (oldValue == null && newValue == null) {
                        return false;
                    } else {
                        return !oldValue.equals(newValue);
                    }
                })
                .toList();
    }
}
