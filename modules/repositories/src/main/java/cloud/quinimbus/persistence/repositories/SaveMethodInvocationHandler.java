package cloud.quinimbus.persistence.repositories;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.entity.Entity;
import cloud.quinimbus.persistence.api.entity.EntityReader;
import cloud.quinimbus.persistence.api.entity.EntityReaderInitialisationException;
import cloud.quinimbus.reflection.GenericMethod;
import java.lang.reflect.Method;

public class SaveMethodInvocationHandler extends RepositoryMethodInvocationHandler {

    private final EntityReader entityReader;

    public SaveMethodInvocationHandler(Class<?> iface, Method m, PersistenceContext ctx)
            throws InvalidRepositoryDefinitionException {
        super(iface, m, ctx);
        if (m.getParameterCount() != 1) {
            throw new InvalidRepositoryDefinitionException("The save method should only have one parameter");
        }
        if (!void.class.equals(m.getReturnType())) {
            throw new InvalidRepositoryDefinitionException("The save method has to be void");
        }
        var parameterType = new GenericMethod(iface, m).getActualParameterType(0);
        if (parameterType.equals(Entity.class)) {
            throw new InvalidRepositoryDefinitionException("Saving entities directly is not yet supported");
        }
        if (parameterType.isRecord()) {
            try {
                var entityType = this.getEntityType();
                this.entityReader = ctx.getRecordEntityReader(entityType, (Class<? extends Record>) parameterType);
                return;
            } catch (EntityReaderInitialisationException ex) {
                throw new InvalidRepositoryDefinitionException(
                        "Exception while creating reader for the repository", ex);
            }
        }
        throw new InvalidRepositoryDefinitionException(
                "Unknown save parameter type: %s".formatted(parameterType.getName()));
    }

    @Override
    public Object invoke(Object proxy, Object[] args) throws Throwable {
        var entityRecord = args[0];
        var entity = this.entityReader.read(entityRecord);
        this.getSchemaStorage().save(entity);
        return null;
    }
}
