package cloud.quinimbus.persistence.repositories;

import cloud.quinimbus.persistence.api.PersistenceContext;
import java.lang.reflect.Proxy;

public class RepositoryFactory {
    
    private final PersistenceContext ctx;

    public RepositoryFactory(PersistenceContext persistenceContext) {
        this.ctx = persistenceContext;
    }
    
    public <T> T createRepositoryInstance(Class<T> iface) throws InvalidRepositoryDefinitionException {
        return (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class[] {iface}, new RepositoryInvocationHandler(iface, this.ctx));
    }
}
