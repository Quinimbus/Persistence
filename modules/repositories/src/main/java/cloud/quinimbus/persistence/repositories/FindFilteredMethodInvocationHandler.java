package cloud.quinimbus.persistence.repositories;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.entity.EntityWriter;
import cloud.quinimbus.persistence.api.entity.EntityWriterInitialisationException;
import cloud.quinimbus.persistence.api.filter.PropertyFilter;
import cloud.quinimbus.persistence.common.filter.FilterFactory;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FindFilteredMethodInvocationHandler extends RepositoryMethodInvocationHandler {

    private final EntityWriter entityWriter;
    
    private final Class parameterType;

    public FindFilteredMethodInvocationHandler(Class<?> iface, Method m, PersistenceContext ctx) throws InvalidRepositoryDefinitionException {
        super(iface, m, ctx);
        if (m.getParameterCount() != 1) {
            throw new InvalidRepositoryDefinitionException("The findFiltered method should have exactly one parameter");
        }
        this.parameterType = m.getParameterTypes()[0];
        if (!Map.class.isAssignableFrom(this.parameterType) && !this.parameterType.isRecord()) {
            throw new InvalidRepositoryDefinitionException("The findFiltered method should have exactly one parameter of type Map or one parameter with a record type");
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
        Set<? extends PropertyFilter> filters;
        if (Map.class.isAssignableFrom(this.parameterType)) {
            filters = FilterFactory.fromMap((Map<String, Object>) properties);
        } else if (this.parameterType.isRecord()) {
            filters = FilterFactory.fromRecord((Record) properties);
        } else {
            throw new IllegalArgumentException("unknown parameter type: " + this.parameterType.getName());
        }
        return this.getSchemaStorage().findFiltered(this.getEntityType(), filters)
                .map(this.entityWriter::write)
                .collect(Collectors.toList());
    }
}
