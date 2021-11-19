package cloud.quinimbus.persistence.repositories;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorage;
import java.lang.reflect.Method;

public class RemoveMethodInvocationHandler extends RepositoryMethodInvocationHandler {

    private final EntityType entityType;
    
    private final PersistenceSchemaStorage schemaStorage;

    public RemoveMethodInvocationHandler(Class<?> iface, Method m, PersistenceContext ctx) throws InvalidRepositoryDefinitionException {
        super(iface, m, ctx);
        if (m.getParameterCount() != 1) {
            throw new InvalidRepositoryDefinitionException("The remove method should only have one parameter");
        }
        if (!void.class.equals(m.getReturnType())) {
            throw new InvalidRepositoryDefinitionException("The remove method has to be void");
        }
        var entityClass = this.getEntityClass();
        if (entityClass.isRecord()) {
            var schema = this.getSchema();
            this.entityType = this.getEntityType();
            this.schemaStorage = this.getSchemaStorage();
        } else {
            throw new InvalidRepositoryDefinitionException("Entity type class for a remove method has to be a record: %s"
                        .formatted(entityClass.getName()));
        }
    }

    @Override
    public Object invoke(Object proxy, Object[] args) throws Throwable {
        var entityId = args[0];
        this.schemaStorage.remove(this.entityType, entityId);
        return null;
    }
}
