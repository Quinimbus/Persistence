package cloud.quinimbus.persistence.repositories;

import cloud.quinimbus.persistence.api.PersistenceContext;
import java.lang.reflect.Method;

public class RemoveMethodInvocationHandler extends RepositoryMethodInvocationHandler {

    public RemoveMethodInvocationHandler(Class<?> iface, Method m, PersistenceContext ctx) throws InvalidRepositoryDefinitionException {
        super(iface, m, ctx);
        if (getEntityType().owningEntity().isEmpty()) {
            if (m.getParameterCount() != 1) {
                throw new InvalidRepositoryDefinitionException("The remove method should have one parameter");
            }
        } else {
            if (m.getParameterCount() != 2) {
                throw new InvalidRepositoryDefinitionException("The remove method should have two parameters for weak entity types");
            }
        }
        if (!void.class.equals(m.getReturnType())) {
            throw new InvalidRepositoryDefinitionException("The remove method has to be void");
        }
    }

    @Override
    public Object invoke(Object proxy, Object[] args) throws Throwable {
        var entityId = args[0];
        this.getSchemaStorage().remove(this.getEntityType(), entityId);
        return null;
    }
}
