package cloud.quinimbus.persistence.repositories;

import cloud.quinimbus.common.tools.Records;
import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.annotation.EntityTypeClass;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.Schema;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorage;
import cloud.quinimbus.reflection.GenericMethod;
import cloud.quinimbus.tools.throwing.ThrowingOptional;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.Getter;

public abstract class RepositoryMethodInvocationHandler {

    private final Class<?> iface;

    private final Method method;

    private final PersistenceContext persistenceContext;

    @Getter(AccessLevel.PROTECTED)
    private final Class<?> entityClass;

    @Getter(AccessLevel.PROTECTED)
    private final Class<?> idClass;

    @Getter(AccessLevel.PROTECTED)
    private final Schema schema;

    @Getter(AccessLevel.PROTECTED)
    private final PersistenceSchemaStorage schemaStorage;

    @Getter(AccessLevel.PROTECTED)
    private final EntityType entityType;

    @Getter(AccessLevel.PROTECTED)
    private final Class<? extends Record> owningTypeRecord;

    @Getter(AccessLevel.PROTECTED)
    private final Function<Record, Object> owningTypeIdGetter;

    public RepositoryMethodInvocationHandler(Class<?> iface, Method method, PersistenceContext persistenceContext)
            throws InvalidRepositoryDefinitionException {
        this.iface = iface;
        this.method = method;
        this.persistenceContext = persistenceContext;
        var entityTypeClassAnno = ThrowingOptional.ofNullable(
                        this.method.getAnnotation(EntityTypeClass.class), InvalidRepositoryDefinitionException.class)
                .or(() -> ThrowingOptional.ofNullable(
                        this.iface.getAnnotation(EntityTypeClass.class), InvalidRepositoryDefinitionException.class))
                .orElseThrow(() -> new InvalidRepositoryDefinitionException(
                        "Missing @EntityTypeClass annotation on method %s in interface %s or on the interface"
                                .formatted(this.method.toString(), this.iface.getName())));
        this.entityClass = entityTypeClassAnno.value();
        this.idClass = entityTypeClassAnno.idClass();
        var entityAnno = this.entityClass.getAnnotation(cloud.quinimbus.persistence.api.annotation.Entity.class);
        this.schema = this.persistenceContext
                .getSchema(entityAnno.schema().id())
                .orElseThrow(() -> new InvalidRepositoryDefinitionException(
                        "Cannot find the defined schema %s in the persistence context"
                                .formatted(entityAnno.schema().id())));
        this.schemaStorage = this.persistenceContext
                .getSchemaStorage(schema.id())
                .orElseThrow(() ->
                        new IllegalStateException("Cannot find the storage for the schema %s".formatted(schema.id())));
        if (entityClass.isRecord()) {
            var recordClass = (Class<? extends Record>) entityClass;
            var typeId = Records.idFromRecordClass(recordClass);
            this.entityType = Optional.ofNullable(schema.entityTypes().get(typeId))
                    .orElseThrow(() -> new InvalidRepositoryDefinitionException(
                            "Cannot find the entity type %s in the schema %s".formatted(typeId, schema.id())));
            if (this.entityType.owningEntity().isPresent()) {
                var genericMethod = new GenericMethod(iface, this.method);
                this.owningTypeRecord = (Class<? extends Record>) genericMethod.getActualParameterType(0);
                this.owningTypeIdGetter = (Function<Record, Object>)
                        persistenceContext.getRecordEntityRegistry().getIdValueGetter(this.owningTypeRecord);
            } else {
                this.owningTypeRecord = null;
                this.owningTypeIdGetter = null;
            }
        } else {
            throw new IllegalArgumentException("Just record classes are supported at the moment");
        }
    }

    public abstract Object invoke(Object proxy, Object[] args) throws Throwable;
}
