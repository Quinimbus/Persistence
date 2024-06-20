package cloud.quinimbus.persistence.repositories;

import java.lang.reflect.Method;

public record InterfaceMethod(Class<?> cls, Method method) {}
