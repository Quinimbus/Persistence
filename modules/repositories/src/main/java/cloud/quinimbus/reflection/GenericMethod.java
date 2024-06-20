package cloud.quinimbus.reflection;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Optional;

public class GenericMethod {

    private final Class<?> implementation;

    private final Method method;

    public GenericMethod(Class<?> implementation, Method method) {
        this.implementation = implementation;
        this.method = method;
    }

    public Class<?> getActualReturnType() {
        var generic = this.method.getGenericReturnType();
        if (generic instanceof TypeVariable tv) {
            return this.resolveTypeVar(tv)
                    .orElseThrow(() ->
                            new IllegalStateException("Cannot resolve the type variable %s for method %s of type %s"
                                    .formatted(tv.toString(), this.method.toString(), this.implementation.toString())));
        }
        return this.method.getReturnType();
    }

    public Class<?> getActualParameterType(int i) {
        var generic = this.method.getGenericParameterTypes()[i];
        if (generic instanceof TypeVariable tv) {
            return this.resolveTypeVar(tv)
                    .orElseThrow(() ->
                            new IllegalStateException("Cannot resolve the type variable %s for method %s of type %s"
                                    .formatted(tv.toString(), this.method.toString(), this.implementation.toString())));
        }
        return this.method.getParameterTypes()[i];
    }

    private Optional<Class<?>> resolveTypeVar(TypeVariable tv) {
        var genericDeclarition = tv.getGenericDeclaration();
        for (Type genericInterface : this.implementation.getGenericInterfaces()) {
            if (genericInterface instanceof ParameterizedType pt) {
                if (genericDeclarition.equals(pt.getRawType())) {
                    var typeParameters = genericDeclarition.getTypeParameters();
                    for (int i = 0; i < typeParameters.length; i++) {
                        if (typeParameters[i].equals(tv)) {
                            if (pt.getActualTypeArguments()[i] instanceof Class c) {
                                return Optional.of(c);
                            }
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }
}
