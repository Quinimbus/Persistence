package cloud.quinimbus.persistence.api.storage;

import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.entity.Entity;
import cloud.quinimbus.persistence.api.schema.EntityType;
import java.util.Optional;
import name.falgout.jeffrey.throwing.stream.ThrowingStream;

public interface PersistenceSchemaStorage {

    <K> void save(Entity<K> entity) throws PersistenceException;

    <K> Optional<Entity<K>> find(EntityType type, K id) throws PersistenceException;

    <K> void remove(EntityType type, K id) throws PersistenceException;

    <K> ThrowingStream<Entity<K>, PersistenceException> findAll(EntityType type) throws PersistenceException;
}
