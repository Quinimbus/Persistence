package cloud.quinimbus.persistence.storage.mongo;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.entity.Entity;
import cloud.quinimbus.persistence.api.entity.UnparseableValueException;
import cloud.quinimbus.persistence.api.filter.PropertyFilter;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.Schema;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorage;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import name.falgout.jeffrey.throwing.stream.ThrowingStream;
import org.bson.Document;

public class MongoSchemaStorage implements PersistenceSchemaStorage {

    private final MongoDatabase database;
    
    private final PersistenceContext context;

    public MongoSchemaStorage(MongoClient client, Schema schema, PersistenceContext context) {
        this.database = client.getDatabase(schema.id());
        this.context = context;
        schema.entityTypes().values()
                .forEach(et -> {
                    var collection = this.database.getCollection(et.id());
                    if (collection == null) {
                        this.database.createCollection(et.id());
                    }
                });
    }

    @Override
    public <K> Optional<Entity<K>> find(EntityType type, K id) throws PersistenceException {
        var collection = this.database.getCollection(type.id());
        var idDocument = new Document(Map.of("_id", id));
        var resultIterator = collection.find(idDocument).iterator();
        if (resultIterator.hasNext()) {
            var doc = resultIterator.next();
            return Optional.of(this.docToEntity(type, id, doc));
        } else {
            return Optional.empty();
        }
    }
    
    @Override
    public <K> ThrowingStream<Entity<K>, PersistenceException> findFiltered(EntityType type, Set<? extends PropertyFilter> propertyFilters) {
        var collection = this.database.getCollection(type.id());
        var propertyMap = propertyFilters.stream()
                .collect(
                        Collectors.toMap(
                                pf -> pf.property(),
                                pf -> pf.value()));
        return ThrowingStream.of(
                StreamSupport.stream(collection.find(new Document(propertyMap)).spliterator(), false),
                PersistenceException.class)
                .map(doc -> this.docToEntity(type, (K)doc.get("_id"), doc));
    }

    @Override
    public <K> void save(Entity<K> entity) throws PersistenceException {
        var map = new LinkedHashMap<>(entity.asBasicMap());
        map.put("_id", entity.getId());
        var collection = this.database.getCollection(entity.getType().id());
        var idDocument = new Document(Map.of("_id", entity.getId()));
        if (collection.find(idDocument).iterator().hasNext()) {
            collection.replaceOne(idDocument, new Document(map));
        } else {
            collection.insertOne(new Document(map));
        }
    }

    @Override
    public <K> ThrowingStream<Entity<K>, PersistenceException> findAll(EntityType type) throws PersistenceException {
        var collection = this.database.getCollection(type.id());
        return ThrowingStream.of(
                StreamSupport.stream(collection.find().spliterator(), false),
                PersistenceException.class)
                .map(doc -> this.docToEntity(type, (K)doc.get("_id"), doc));
    }

    private <K> Entity<K> docToEntity(EntityType type, K id, Document doc) throws UnparseableValueException {
        doc.remove("_id");
        return this.context.newEntity(id, type, doc);
    }

    @Override
    public <K> void remove(EntityType type, K id) throws PersistenceException {
        var collection = this.database.getCollection(type.id());
        var idDocument = new Document(Map.of("_id", id));
        collection.deleteOne(idDocument);
    }
}
