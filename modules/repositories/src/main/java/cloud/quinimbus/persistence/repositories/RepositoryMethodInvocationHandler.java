package cloud.quinimbus.persistence.repositories;

import cloud.quinimbus.common.tools.Records;
import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.annotation.EntityTypeClass;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.Schema;
import cloud.quinimbus.persistence.api.storage.PersistenceSchemaStorage;
import cloud.quinimbus.tools.throwing.ThrowingOptional;
import java.lang.reflect.Method;
import java.util.Optional;

public abstract class RepositoryMethodInvocationHandler {
    
    private final Class<?> iface;
    
    private final Method method;
    
    private final PersistenceContext persistenceContext;

    public RepositoryMethodInvocationHandler(Class<?> iface, Method method, PersistenceContext persistenceContext) {
        this.iface = iface;
        this.method = method;
        this.persistenceContext = persistenceContext;
    }

    public abstract Object invoke(Object proxy, Object[] args) throws Throwable;
    
    protected Class<?> getEntityClass() throws InvalidRepositoryDefinitionException {
        return ThrowingOptional.ofNullable(this.method.getAnnotation(EntityTypeClass.class), InvalidRepositoryDefinitionException.class)
                    .or(() -> ThrowingOptional.ofNullable(this.iface.getAnnotation(EntityTypeClass.class), InvalidRepositoryDefinitionException.class))
                    .orElseThrow(() -> new InvalidRepositoryDefinitionException("Missing @EntityTypeClass annotation on method %s in class %s".formatted(this.method.toString(), this.iface.getName())))
                .value();
    }
    
    protected Schema getSchema() throws InvalidRepositoryDefinitionException {
        var entityAnno = this.getEntityClass()
                .getAnnotation(cloud.quinimbus.persistence.api.annotation.Entity.class);
        return this.persistenceContext.getSchema(entityAnno.schema().id())
                .orElseThrow(() ->
                        new InvalidRepositoryDefinitionException("Cannot find the defined schema %s in the persistence context"
                                .formatted(entityAnno.schema().id())));
    }
    
    protected PersistenceSchemaStorage getSchemaStorage() throws InvalidRepositoryDefinitionException {
        var schema = this.getSchema();
        return this.persistenceContext.getSchemaStorage(schema.id())
                .orElseThrow(() -> new IllegalStateException("Cannot find the storage for the schema %s".formatted(schema.id())));
    }
    
    protected EntityType getEntityType() throws InvalidRepositoryDefinitionException {
        var schema = this.getSchema();
        var entityClass = this.getEntityClass();
        if (entityClass.isRecord()) {
            var recordClass = (Class<? extends Record>)entityClass;
            var typeId = Records.idFromRecordClass(recordClass);
            return Optional.ofNullable(schema.entityTypes().get(typeId))
                                .orElseThrow(() ->
                                        new InvalidRepositoryDefinitionException("Cannot find the entity type %s in the schema %s"
                                                .formatted(typeId, schema.id())));
        }
        throw new IllegalArgumentException("Just record classes are supported at the moment");
    }
}
