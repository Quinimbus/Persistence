package cloud.quinimbus.persistence.lifecycle;

import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.entity.Entity;
import cloud.quinimbus.persistence.api.filter.PropertyFilter;
import cloud.quinimbus.persistence.api.lifecycle.EntityPostLoadEvent;
import cloud.quinimbus.persistence.api.lifecycle.EntityPostSaveEvent;
import cloud.quinimbus.persistence.api.lifecycle.EntityPreSaveEvent;
import cloud.quinimbus.persistence.api.lifecycle.LifecycleEvent;
import cloud.quinimbus.persistence.api.lifecycle.diff.CompletePropertyDiff;
import cloud.quinimbus.persistence.api.lifecycle.diff.Diff;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorage;
import cloud.quinimbus.persistence.common.storage.PersistenceSchemaStorageDelegate;
import cloud.quinimbus.persistence.entity.DefaultEntity;
import cloud.quinimbus.persistence.lifecycle.diff.EntityComparator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import name.falgout.jeffrey.throwing.stream.ThrowingStream;
import org.apache.commons.collections4.SetUtils;

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

    private record PreSaveResult<K>(Entity<K> mutatedEntity, Set<Diff<Object>> diffs) {}

    @Override
    public <K> void save(Entity<K> entity) throws PersistenceException {
        var preSaveResult = this.callPreSaveEventConsumerRounds(entity);
        super.save(preSaveResult.mutatedEntity());
        var loadedEntity = super.find(entity.getType(), entity.getId()).orElseThrow();
        this.callPostSaveEventConsumers(new DefaultEntity<>(loadedEntity), preSaveResult.diffs());
    }

    @Override
    public <K> Optional<Entity<K>> find(EntityType type, K id) throws PersistenceException {
        return super.find(type, id).map(this::callPostLoadEventConsumers);
    }

    @Override
    public <K> ThrowingStream<Entity<K>, PersistenceException> findAll(EntityType type) throws PersistenceException {
        return super.<K>findAll(type).map(this::callPostLoadEventConsumers);
    }

    @Override
    public <K> ThrowingStream<Entity<K>, PersistenceException> findFiltered(
            EntityType type, Set<? extends PropertyFilter> propertyFilters) {
        return super.<K>findFiltered(type, propertyFilters).map(this::callPostLoadEventConsumers);
    }

    private <K> PreSaveResult<K> callPreSaveEventConsumerRounds(Entity<K> entity) throws PersistenceException {
        Set<Diff<Object>> diffs = new HashSet<>();
        super.find(entity.getType(), entity.getId())
                .ifPresentOrElse(
                        oldEntity -> {
                            diffs.addAll(EntityComparator.compareEntities(entity, oldEntity));
                        },
                        () -> {
                            diffs.addAll(entity.getProperties().entrySet().stream()
                                    .map(e -> new CompletePropertyDiff<>(e.getKey(), null, e.getValue()))
                                    .collect(Collectors.toSet()));
                        });
        int preSaveRoundCount = 0;
        var entityOfLastRound = entity;
        var mutatedPropertiesInLastRound = diffs;
        while (!mutatedPropertiesInLastRound.isEmpty()) {
            var preSaveResult = this.callPreSaveEventConsumers(
                    new DefaultEntity<>(entityOfLastRound), mutatedPropertiesInLastRound);
            entityOfLastRound = preSaveResult.mutatedEntity();
            mutatedPropertiesInLastRound = preSaveResult.diffs();
            diffs.addAll(mutatedPropertiesInLastRound);
            preSaveRoundCount++;
            if (preSaveRoundCount > 100) {
                throw new IllegalStateException(
                        "There seems to be an endless recursion in your pre-save event handlers");
            }
        }
        return new PreSaveResult<>(entityOfLastRound, diffs);
    }

    private <K> PreSaveResult<K> callPreSaveEventConsumers(Entity<K> entity, Set<Diff<Object>> diffs) {
        return Optional.ofNullable(this.consumers.get(EntityPreSaveEvent.class))
                .map(m -> m.get(entity.getType().id()))
                .map(cl -> cl.stream()
                        .map(c -> (Consumer<EntityPreSaveEvent<K>>) c)
                        .map(c -> this.callPreSaveEventConsumer(entity, diffs, c))
                        .collect(Collectors.reducing(
                                new PreSaveResult<>(entity, Set.of()), this::combinePreSaveResults)))
                .orElseGet(() -> new PreSaveResult<>(entity, Set.of()));
    }

    private <K> PreSaveResult<K> callPreSaveEventConsumer(
            Entity<K> entity, Set<Diff<Object>> diffs, Consumer<EntityPreSaveEvent<K>> consumer) {
        var mutatedEntity = new AtomicReference<>(entity);
        consumer.accept(new EntityPreSaveEvent<>(new DefaultEntity<>(entity), diffs, mutatedEntity::set));
        return new PreSaveResult<>(mutatedEntity.get(), EntityComparator.compareEntities(mutatedEntity.get(), entity));
    }

    private <K> PreSaveResult<K> combinePreSaveResults(PreSaveResult<K> res1, PreSaveResult<K> res2) {
        var intersection = SetUtils.intersection(res1.diffs(), res2.diffs());
        if (!intersection.isEmpty()) {
            throw new IllegalStateException(
                    "Two event handlers have changed the same field or multiple fields (%s) in the same round"
                            .formatted(intersection.stream().map(Diff::name).collect(Collectors.joining(", "))));
        }
        var result = new DefaultEntity<>(res1.mutatedEntity());
        for (var diff : res2.diffs()) {
            result.setProperty(diff.name(), diff.newValue());
        }
        return new PreSaveResult<>(result, SetUtils.union(res1.diffs(), res2.diffs()));
    }

    private <K> void callPostSaveEventConsumers(Entity<K> entity, Set<Diff<Object>> diffs) {
        var postSaveEvent = new EntityPostSaveEvent<K>(entity, diffs);
        Optional.ofNullable(this.consumers.get(EntityPostSaveEvent.class))
                .map(m -> m.get(entity.getType().id()))
                .ifPresent(cl -> {
                    cl.stream().map(c -> (Consumer<EntityPostSaveEvent<K>>) c).forEach(c -> c.accept(postSaveEvent));
                });
    }

    private <K> Entity<K> callPostLoadEventConsumers(Entity<K> entity) {
        var entityRef = new AtomicReference<>(entity);
        Optional.ofNullable(this.consumers.get(EntityPostLoadEvent.class))
                .map(m -> m.get(entity.getType().id()))
                .ifPresent(cl -> {
                    cl.stream()
                            .map(c -> (Consumer<EntityPostLoadEvent<K>>) c)
                            .forEach(c -> c.accept(new EntityPostLoadEvent<>(entityRef.get(), entityRef::set)));
                });
        return entityRef.get();
    }
}
