package cloud.quinimbus.persistence.repositories;

import cloud.quinimbus.persistence.api.PersistenceContext;
import java.lang.reflect.Method;

public class FindMethodInvocationHandler extends RepositoryMethodInvocationHandler {
    
    private final RepositoryMethodInvocationHandler delegate;

    public FindMethodInvocationHandler(Class<?> iface, Method m, PersistenceContext ctx) throws InvalidRepositoryDefinitionException {
        super(iface, m, ctx);
        var name = m.getName();
        if ("findAll".equals(name)) {
            this.delegate = new FindAllMethodInvocationHandler(iface, m, ctx);
        } else if ("findAllIDs".equals(name)) {
            this.delegate = new FindAllIDsMethodInvocationHandler(iface, m, ctx);
        } else if ("findOne".equals(name)) {
            this.delegate = new FindOneMethodInvocationHandler(iface, m, ctx);
        } else if ("findFiltered".equals(name)) {
            this.delegate = new FindFilteredMethodInvocationHandler(iface, m, ctx);
        } else if ("findIDsFiltered".equals(name)) {
            this.delegate = new FindIDsFilteredMethodInvocationHandler(iface, m, ctx);
        } else if (name.startsWith("findBy") || name.startsWith("findAllBy") || name.startsWith("findOneBy") || name.startsWith("findFilteredBy")) {
            this.delegate = new FindFilteredByMethodNameInvocationHandler(iface, m, ctx);
        } else {
            throw new InvalidRepositoryDefinitionException("Cannot understand find method name %s".formatted(name));
        }
    }

    @Override
    public Object invoke(Object proxy, Object[] args) throws Throwable {
        return this.delegate.invoke(proxy, args);
    }
}
