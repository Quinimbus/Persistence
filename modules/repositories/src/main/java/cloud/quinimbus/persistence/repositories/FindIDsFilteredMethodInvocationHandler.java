package cloud.quinimbus.persistence.repositories;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.filter.PropertyFilter;
import cloud.quinimbus.persistence.common.filter.FilterFactory;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FindIDsFilteredMethodInvocationHandler extends RepositoryMethodInvocationHandler {
    
    private final Class parameterType;

    public FindIDsFilteredMethodInvocationHandler(Class<?> iface, Method m, PersistenceContext ctx) throws InvalidRepositoryDefinitionException {
        super(iface, m, ctx);
        if (this.getEntityType().owningEntity().isEmpty()) {
            if (m.getParameterCount() != 1) {
                throw new InvalidRepositoryDefinitionException("The findFiltered method should have exactly one parameter");
            }
            this.parameterType = m.getParameterTypes()[0];
        } else {
            if (m.getParameterCount() != 2) {
                throw new InvalidRepositoryDefinitionException("The findFiltered method should have exactly two parameters for weak entities");
            }
            this.parameterType = m.getParameterTypes()[1];
        }
        if (!Map.class.isAssignableFrom(this.parameterType) && !this.parameterType.isRecord()) {
            throw new InvalidRepositoryDefinitionException("The last parameter of the findFiltered method should be of type Map or be a record type");
        }
        var returnType = m.getReturnType();
        if (List.class.equals(returnType)) {
            
        } else {
            throw new InvalidRepositoryDefinitionException("Unknown return type for a findIDsFiltered method: %s"
                    .formatted(returnType.getName()));
        }
    }

    @Override
    public Object invoke(Object proxy, Object[] args) throws Throwable {
        var properties = args[getEntityType().owningEntity().isEmpty() ? 0 : 1];
        Set<? extends PropertyFilter> filters;
        if (Map.class.isAssignableFrom(this.parameterType)) {
            filters = FilterFactory.fromMap((Map<String, Object>) properties);
        } else if (this.parameterType.isRecord()) {
            filters = FilterFactory.fromRecord((Record) properties);
        } else {
            throw new IllegalArgumentException("unknown parameter type: " + this.parameterType.getName());
        }
        if (getEntityType().owningEntity().isEmpty()) {
            return this.getSchemaStorage().findIDsFiltered(this.getEntityType(), filters)
                    .collect(Collectors.toList());
        } else {
            var owner = this.getOwningTypeRecord().cast(args[0]);
            var ownerId = this.getOwningTypeIdGetter().apply(owner);
            var extendedFilters = new HashSet<PropertyFilter>(filters);
            extendedFilters.add(FilterFactory.filterEquals(
                            this.getEntityType().owningEntity().orElseThrow().field(),
                            ownerId));
            return this.getSchemaStorage().findIDsFiltered(this.getEntityType(), extendedFilters)
                    .collect(Collectors.toList());
        }
    }
}
