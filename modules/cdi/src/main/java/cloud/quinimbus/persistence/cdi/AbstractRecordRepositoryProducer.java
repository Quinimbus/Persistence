package cloud.quinimbus.persistence.cdi;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.repositories.InvalidRepositoryDefinitionException;
import cloud.quinimbus.persistence.repositories.RepositoryFactory;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

public abstract class AbstractRecordRepositoryProducer {

    @Inject
    private PersistenceContext persistenceContext;

    private RepositoryFactory repositoryFactory;

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
