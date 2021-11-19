package cloud.quinimbus.persistence.cdi;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.schema.InvalidSchemaException;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorage;
import cloud.quinimbus.persistence.api.storage.PersistenceStorageProvider;
import cloud.quinimbus.persistence.repositories.InvalidRepositoryDefinitionException;
import cloud.quinimbus.persistence.repositories.RepositoryFactory;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

public abstract class AbstractRecordRepositoryProducer {

    @Inject
    private PersistenceContext persistenceContext;

    private PersistenceSchemaStorage storage;

    private RepositoryFactory repositoryFactory;

    @PostConstruct
    public void init() {
        try {
            var schemaProvider = this.persistenceContext.importRecordSchema(this.recordClasses());
            var schema = schemaProvider.getSchemas().stream().findFirst().orElseThrow();
            var storageProvider = this.storageProvider();
            this.storage = storageProvider.createSchema(this.persistenceContext, schema);
            this.persistenceContext.setSchemaStorage(schema.id(), this.storage);
            this.repositoryFactory = new RepositoryFactory(this.persistenceContext);
        } catch (InvalidSchemaException ex) {
            throw new IllegalStateException("Cannot initialize persistence schema", ex);
        }
    }

    public PersistenceSchemaStorage getStorage() {
        return this.storage;
    }

    public PersistenceContext getPersistenceContext() {
        return this.persistenceContext;
    }

    public <T> T getRepository(Class<T> iface) {
        try {
            return this.repositoryFactory.createRepositoryInstance(iface);
        } catch (InvalidRepositoryDefinitionException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public abstract Class<? extends Record>[] recordClasses();

    public abstract PersistenceStorageProvider storageProvider();
}
