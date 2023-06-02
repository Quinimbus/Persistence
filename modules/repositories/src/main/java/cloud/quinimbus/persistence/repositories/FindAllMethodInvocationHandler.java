package cloud.quinimbus.persistence.repositories;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.entity.EntityWriter;
import cloud.quinimbus.persistence.api.entity.EntityWriterInitialisationException;
import cloud.quinimbus.persistence.api.filter.PropertyFilter;
import cloud.quinimbus.persistence.common.filter.FilterFactory;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FindAllMethodInvocationHandler extends RepositoryMethodInvocationHandler {

    private final EntityWriter entityWriter;

    public FindAllMethodInvocationHandler(Class<?> iface, Method m, PersistenceContext ctx) throws InvalidRepositoryDefinitionException {
        super(iface, m, ctx);
        if (getEntityType().owningEntity().isEmpty()) {
            if (m.getParameterCount() != 0) {
                throw new InvalidRepositoryDefinitionException("The findAll method should only have zero parameters");
            }
        } else {
            if (m.getParameterCount() != 1) {
                throw new InvalidRepositoryDefinitionException("The findAll method should have one parameter for weak entities");
            }
        }
        var returnType = m.getReturnType();
        if (List.class.equals(returnType)) {
            var genericReturnType = this.getEntityClass();
            if (genericReturnType.isRecord()) {
                try {
                    this.entityWriter = ctx.getRecordEntityWriter(this.getEntityType(), (Class<? extends Record>) genericReturnType);
                } catch (EntityWriterInitialisationException ex) {
                    throw new InvalidRepositoryDefinitionException("Exception while creating writer for the repository", ex);
                }
            } else {
                throw new InvalidRepositoryDefinitionException("Generic return type for a findOne method has to be a record: %s"
                        .formatted(returnType.getName()));
            }
        } else {
            throw new InvalidRepositoryDefinitionException("Unknown return type for a findAll method: %s"
                    .formatted(returnType.getName()));
        }
    }

    @Override
    public Object invoke(Object proxy, Object[] args) throws Throwable {
        if (getEntityType().owningEntity().isEmpty()) {
            return this.getSchemaStorage().findAll(this.getEntityType())
                    .map(this.entityWriter::write)
                    .collect(Collectors.toList());
        } else {
            var owner = this.getOwningTypeRecord().cast(args[0]);
            var ownerId = this.getOwningTypeIdGetter().apply(owner);
            return this.getSchemaStorage().findFiltered(
                    this.getEntityType(),
                    Set.of(new FilterFactory.DefaultPropertyFilter(
                            this.getEntityType().owningEntity().orElseThrow().field(),
                            PropertyFilter.Operator.EQUALS,
                            ownerId)))
                    .map(this.entityWriter::write)
                    .collect(Collectors.toList());
        }
    }
}
