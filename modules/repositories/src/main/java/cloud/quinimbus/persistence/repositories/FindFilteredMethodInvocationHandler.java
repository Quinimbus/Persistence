package cloud.quinimbus.persistence.repositories;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.entity.EntityWriter;
import cloud.quinimbus.persistence.api.entity.EntityWriterInitialisationException;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorage;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FindFilteredMethodInvocationHandler extends RepositoryMethodInvocationHandler {

    private final EntityType entityType;

    private final EntityWriter entityWriter;

    private final PersistenceSchemaStorage schemaStorage;

    public FindFilteredMethodInvocationHandler(Class<?> iface, Method m, PersistenceContext ctx) throws InvalidRepositoryDefinitionException {
        super(iface, m, ctx);
        if (m.getParameterCount() != 1) {
            throw new InvalidRepositoryDefinitionException("The findFiltered method should have exactly one parameter");
        }
        if (!Map.class.isAssignableFrom(m.getParameterTypes()[0])) {
            throw new InvalidRepositoryDefinitionException("The findFiltered method should have exactly one parameter of type Map");
        }
        var returnType = m.getReturnType();
        if (List.class.equals(returnType)) {
            var genericReturnType = this.getEntityClass();
            if (genericReturnType.isRecord()) {
                try {
                    this.entityType = this.getEntityType();
                    this.entityWriter = ctx.getRecordEntityWriter(entityType, (Class<? extends Record>) genericReturnType);
                    this.schemaStorage = this.getSchemaStorage();
                } catch (EntityWriterInitialisationException ex) {
                    throw new InvalidRepositoryDefinitionException("Exception while creating writer for the repository", ex);
                }
            } else {
                throw new InvalidRepositoryDefinitionException("Generic return type for a findFiltered method has to be a record: %s"
                        .formatted(returnType.getName()));
            }
        } else {
            throw new InvalidRepositoryDefinitionException("Unknown return type for a findFiltered method: %s"
                    .formatted(returnType.getName()));
        }
    }

    @Override
    public Object invoke(Object proxy, Object[] args) throws Throwable {
        var properties = args[0];
        return this.schemaStorage.findFiltered(this.entityType, (Map<String, Object>) properties)
                .map(this.entityWriter::write)
                .collect(Collectors.toList());
    }
}
