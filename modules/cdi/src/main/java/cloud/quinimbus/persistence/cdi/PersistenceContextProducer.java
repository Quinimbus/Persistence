package cloud.quinimbus.persistence.cdi;

import cloud.quinimbus.persistence.api.PersistenceContext;
import java.util.ServiceLoader;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class PersistenceContextProducer {

    private PersistenceContext persistenceContext;

    public PersistenceContextProducer() {
        this.persistenceContext = ServiceLoader.load(PersistenceContext.class).findFirst()
                .orElseThrow(() -> new IllegalStateException("Cannot find any PersistenceContext implementation"));
    }

    @Produces
    public PersistenceContext getContext() {
        return this.persistenceContext;
    }
}
