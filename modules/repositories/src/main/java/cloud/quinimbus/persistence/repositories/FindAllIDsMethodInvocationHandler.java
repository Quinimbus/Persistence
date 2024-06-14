package cloud.quinimbus.persistence.repositories;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.filter.PropertyFilter;
import cloud.quinimbus.persistence.common.filter.FilterFactory;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FindAllIDsMethodInvocationHandler extends RepositoryMethodInvocationHandler {

    public FindAllIDsMethodInvocationHandler(Class<?> iface, Method m, PersistenceContext ctx) throws InvalidRepositoryDefinitionException {
        super(iface, m, ctx);
        if (getEntityType().owningEntity().isEmpty()) {
            if (m.getParameterCount() != 0) {
                throw new InvalidRepositoryDefinitionException("The findAllIDs method should only have zero parameters");
            }
        } else {
            if (m.getParameterCount() != 1) {
                throw new InvalidRepositoryDefinitionException("The findAllIDs method should have one parameter for weak entities");
            }
        }
        var returnType = m.getReturnType();
        if (List.class.equals(returnType)) {
            
        } else {
            throw new InvalidRepositoryDefinitionException("Unknown return type for a findAllIDs method: %s"
                    .formatted(returnType.getName()));
        }
    }

    @Override
    public Object invoke(Object proxy, Object[] args) throws Throwable {
        if (getEntityType().owningEntity().isEmpty()) {
            return this.getSchemaStorage().findAllIDs(this.getEntityType())
                    .collect(Collectors.toList());
        } else {
            var owner = this.getOwningTypeRecord().cast(args[0]);
            var ownerId = this.getOwningTypeIdGetter().apply(owner);
            return this.getSchemaStorage().findIDsFiltered(
                    this.getEntityType(),
                    Set.of(new FilterFactory.DefaultPropertyFilter(
                            this.getEntityType().owningEntity().orElseThrow().field(),
                            PropertyFilter.Operator.EQUALS,
                            ownerId)))
                    .collect(Collectors.toList());
        }
    }
}
