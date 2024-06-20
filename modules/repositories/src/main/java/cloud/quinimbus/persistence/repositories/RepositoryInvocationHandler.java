package cloud.quinimbus.persistence.repositories;

import cloud.quinimbus.persistence.api.PersistenceContext;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import name.falgout.jeffrey.throwing.stream.ThrowingStream;

public class RepositoryInvocationHandler implements InvocationHandler {

    private final PersistenceContext ctx;

    private final Class<?> iface;

    private final Map<Method, RepositoryMethodInvocationHandler> methodHandlers;

    public RepositoryInvocationHandler(Class iface, PersistenceContext ctx)
            throws InvalidRepositoryDefinitionException {
        try {
            this.ctx = ctx;
            this.iface = iface;
            if (!iface.isInterface()) {
                throw new InvalidRepositoryDefinitionException("%s is no interface".formatted(iface.getName()));
            }
            this.methodHandlers = ThrowingStream.of(
                            Arrays.stream(iface.getMethods()), InvalidRepositoryDefinitionException.class)
                    .filter(m -> !Object.class.equals(m.getDeclaringClass()))
                    .map(m -> Map.entry(m, this.createMethodHandler(m)))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (InvalidRepositoryDefinitionException ex) {
            throw new InvalidRepositoryDefinitionException(
                    "Error while creating the repository for the interface %s".formatted(iface.getName()), ex);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return this.methodHandlers.get(method).invoke(proxy, args);
    }

    private RepositoryMethodInvocationHandler createMethodHandler(Method m)
            throws InvalidRepositoryDefinitionException {
        var name = m.getName();
        if (name.startsWith("find")) {
            return new FindMethodInvocationHandler(this.iface, m, this.ctx);
        } else if (name.equals("save")) {
            return new SaveMethodInvocationHandler(this.iface, m, this.ctx);
        } else if (name.equals("remove")) {
            return new RemoveMethodInvocationHandler(this.iface, m, this.ctx);
        }
        throw new InvalidRepositoryDefinitionException("cannot understand the method name %s".formatted(name));
    }
}
