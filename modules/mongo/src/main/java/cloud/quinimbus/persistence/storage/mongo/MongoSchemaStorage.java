package cloud.quinimbus.persistence.storage.mongo;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.entity.Entity;
import cloud.quinimbus.persistence.api.entity.StructuredObjectEntry;
import cloud.quinimbus.persistence.api.entity.UnparseableValueException;
import cloud.quinimbus.persistence.api.filter.PropertyFilter;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.EntityTypePropertyType;
import cloud.quinimbus.persistence.api.schema.Metadata;
import cloud.quinimbus.persistence.api.schema.Schema;
import cloud.quinimbus.persistence.api.schema.properties.LocalDatePropertyType;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorage;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import name.falgout.jeffrey.throwing.stream.ThrowingStream;
import org.bson.Document;

public class MongoSchemaStorage implements PersistenceSchemaStorage {
    
    private final String SCHEMA_COLLECTION_NAME = "_qn_schemameta";
    
    private final String SCHEMA_COLLECTION_METADATA_ID = "_qn_schemameta_id";
    
    private final Document SCHEMA_COLLECTION_METADATA_ID_DOCUMENT = new Document(Map.of("_id", SCHEMA_COLLECTION_METADATA_ID));

    private final MongoDatabase database;
    
    private final PersistenceContext context;
    
    private final MongoCollection<Document> schemaMetadataCollection;

    public MongoSchemaStorage(MongoClient client, Schema schema, PersistenceContext context) throws PersistenceException {
        this.database = client.getDatabase(schema.id());
        this.context = context;
        this.schemaMetadataCollection = Optional.ofNullable(this.database.getCollection(SCHEMA_COLLECTION_NAME))
                .orElseGet(() -> {
                    this.database.createCollection(SCHEMA_COLLECTION_NAME);
                    return this.database.getCollection(SCHEMA_COLLECTION_NAME);
                });
        var existingSchemaMetadata = this.loadSchemaMetadata();
        if (existingSchemaMetadata.isPresent()) {
            if (!existingSchemaMetadata.get().id().equals(schema.id())) {
                throw new PersistenceException("The given mongo database already contains a quinimbus schema named %s, you cannot create the schema %s".formatted(existingSchemaMetadata.get().id(), schema.id()));
            }
            if (existingSchemaMetadata.get().version() > schema.version()) {
                throw new PersistenceException("The given mongo database already contains a quinimbus schema named %s in version %d, you cannot read it as version %d".formatted(existingSchemaMetadata.get().id(), existingSchemaMetadata.get().version(), schema.version()));
            }
        } else {
            this.saveSchemaMetadata(new Metadata(schema.id(), schema.version(), Instant.now()));
        }
        schema.entityTypes().values()
                .forEach(et -> {
                    var collection = this.database.getCollection(et.id());
                    if (collection == null) {
                        this.database.createCollection(et.id());
                    }
                });
    }
    
    private Optional<Metadata> loadSchemaMetadata() {
        var resultIter = this.schemaMetadataCollection.find(SCHEMA_COLLECTION_METADATA_ID_DOCUMENT).iterator();
        if (resultIter.hasNext()) {
            var result = resultIter.next();
            return Optional.of(new Metadata(result.getString("schemaId"), result.getLong("schemaVersion"), Instant.parse(result.getString("creationTime"))));
        } else {
            return Optional.empty();
        }
    }
    
    private void saveSchemaMetadata(Metadata metadata) {
        var map = Map.<String, Object>of(
                "_id", SCHEMA_COLLECTION_METADATA_ID,
                "schemaId", metadata.id(),
                "schemaVersion", metadata.version(),
                "creationTime", metadata.creationTime().toString());
        if (this.schemaMetadataCollection.find(SCHEMA_COLLECTION_METADATA_ID_DOCUMENT).iterator().hasNext()) {
            schemaMetadataCollection.replaceOne(SCHEMA_COLLECTION_METADATA_ID_DOCUMENT, new Document(map));
        } else {
            schemaMetadataCollection.insertOne(new Document(map));
        }
    }

    @Override
    public Metadata getSchemaMetadata() throws PersistenceException {
        return this.loadSchemaMetadata().orElseThrow();
    }

    @Override
    public void increaseSchemaVersion(Long version) throws PersistenceException {
        var metadata = this.getSchemaMetadata();
        if (metadata.version() > version) {
            throw new PersistenceException("You cannot downgrade the schema version");
        }
        this.saveSchemaMetadata(metadata.withVersion(version));
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
        var map = new LinkedHashMap<>(entity.asBasicMap(this::convertProperty));
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
    
    private Object convertProperty(StructuredObjectEntry<EntityTypePropertyType> entry) {
        if (entry.type() instanceof LocalDatePropertyType) {
            return ((LocalDate)entry.value()).format(DateTimeFormatter.ISO_DATE);
        }
        return entry.value();
    }
}
