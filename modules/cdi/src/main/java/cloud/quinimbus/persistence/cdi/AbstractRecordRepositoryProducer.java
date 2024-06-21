package cloud.quinimbus.persistence.cdi;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.repositories.InvalidRepositoryDefinitionException;
import cloud.quinimbus.persistence.repositories.RepositoryFactory;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public abstract class AbstractRecordRepositoryProducer {

    private final PersistenceContext persistenceContext;

    private RepositoryFactory repositoryFactory;

    // to allow CDI proxy creation
    public AbstractRecordRepositoryProducer() {
        this.persistenceContext = null;
    }

    @Inject
    public AbstractRecordRepositoryProducer(PersistenceContext persistenceContext) {
        this.persistenceContext = persistenceContext;
    }

    @PostConstruct
    public void init() {
        this.repositoryFactory = new RepositoryFactory(this.persistenceContext);
    }

    public <T> T getRepository(Class<T> iface) {
        try {
            return this.repositoryFactory.createRepositoryInstance(iface);
        } catch (InvalidRepositoryDefinitionException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
