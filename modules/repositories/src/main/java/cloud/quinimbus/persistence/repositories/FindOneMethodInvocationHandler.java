package cloud.quinimbus.persistence.repositories;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.entity.EntityWriter;
import cloud.quinimbus.persistence.api.entity.EntityWriterInitialisationException;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorage;
import java.lang.reflect.Method;
import java.util.Optional;

public class FindOneMethodInvocationHandler extends RepositoryMethodInvocationHandler {

    private final EntityType entityType;

    private final EntityWriter entityWriter;

    private final PersistenceSchemaStorage schemaStorage;

    public FindOneMethodInvocationHandler(Class<?> iface, Method m, PersistenceContext ctx) throws InvalidRepositoryDefinitionException {
        super(iface, m, ctx);
        if (m.getParameterCount() != 1) {
            throw new InvalidRepositoryDefinitionException("The findOne method should only have one parameter");
        }
        var returnType = m.getReturnType();
        if (Optional.class.equals(returnType)) {
            var genericReturnType = this.getEntityClass();
            if (genericReturnType.isRecord()) {
                try {
                    this.entityType = this.getEntityType();
                    this.entityWriter = ctx
                            .getRecordEntityWriter(entityType, (Class<? extends Record>) genericReturnType);
                    this.schemaStorage = this.getSchemaStorage();
                } catch (EntityWriterInitialisationException ex) {
                    throw new InvalidRepositoryDefinitionException("Exception while creating writer for the repository", ex);
                }
            } else {
                throw new InvalidRepositoryDefinitionException("Generic return type for a findOne method has to be a record: %s"
                        .formatted(returnType.getName()));
            }
        } else {
            throw new InvalidRepositoryDefinitionException("Unknown return type for a findOne method: %s"
                    .formatted(returnType.getName()));
        }
    }

    @Override
    public Object invoke(Object proxy, Object[] args) throws Throwable {
        var key = args[0];
        return this.schemaStorage.find(this.entityType, key)
                .map(this.entityWriter::write);
    }
}
