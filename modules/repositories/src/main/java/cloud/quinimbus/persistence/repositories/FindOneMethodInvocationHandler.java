package cloud.quinimbus.persistence.repositories;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.entity.EntityWriter;
import cloud.quinimbus.persistence.api.entity.EntityWriterInitialisationException;
import java.lang.reflect.Method;
import java.util.Optional;

public class FindOneMethodInvocationHandler extends RepositoryMethodInvocationHandler {

    private final EntityWriter entityWriter;

    public FindOneMethodInvocationHandler(Class<?> iface, Method m, PersistenceContext ctx)
            throws InvalidRepositoryDefinitionException {
        super(iface, m, ctx);
        if (getEntityType().owningEntity().isEmpty()) {
            if (m.getParameterCount() != 1) {
                throw new InvalidRepositoryDefinitionException("The findOne method should have one parameter");
            }
        } else {
            if (m.getParameterCount() != 2) {
                throw new InvalidRepositoryDefinitionException(
                        "The findOne method should have two parameters for weak entities");
            }
        }
        var returnType = m.getReturnType();
        if (Optional.class.equals(returnType)) {
            var genericReturnType = this.getEntityClass();
            if (genericReturnType.isRecord()) {
                try {
                    this.entityWriter = ctx.getRecordEntityWriter(
                            this.getEntityType(), (Class<? extends Record>) genericReturnType);
                } catch (EntityWriterInitialisationException ex) {
                    throw new InvalidRepositoryDefinitionException(
                            "Exception while creating writer for the repository", ex);
                }
            } else {
                throw new InvalidRepositoryDefinitionException(
                        "Generic return type for a findOne method has to be a record: %s"
                                .formatted(returnType.getName()));
            }
        } else {
            throw new InvalidRepositoryDefinitionException(
                    "Unknown return type for a findOne method: %s".formatted(returnType.getName()));
        }
    }

    @Override
    public Object invoke(Object proxy, Object[] args) throws Throwable {
        if (getEntityType().owningEntity().isEmpty()) {
            var key = args[0];
            if (key == null) {
                throw new PersistenceException("The id may not be null");
            }
            return this.getSchemaStorage().find(this.getEntityType(), key).map(this.entityWriter::write);
        } else {
            var owner = this.getOwningTypeRecord().cast(args[0]);
            var ownerId = this.getOwningTypeIdGetter().apply(owner);
            var key = args[1];
            if (key == null) {
                throw new PersistenceException("The id may not be null");
            }
            return this.getSchemaStorage()
                    .find(this.getEntityType(), key)
                    .filter(e -> e.getProperty(
                                    this.getEntityType().owningEntity().get().field())
                            .equals(ownerId))
                    .map(this.entityWriter::write);
        }
    }
}
