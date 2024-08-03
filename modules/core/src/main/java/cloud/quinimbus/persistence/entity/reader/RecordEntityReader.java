package cloud.quinimbus.persistence.entity.reader;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.annotation.EntityIdField;
import cloud.quinimbus.persistence.api.entity.Entity;
import cloud.quinimbus.persistence.api.entity.EntityReader;
import cloud.quinimbus.persistence.api.entity.EntityReaderInitialisationException;
import cloud.quinimbus.persistence.api.entity.UnparseableValueException;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.exception.EntityReaderReadException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class RecordEntityReader<T extends Record> extends AbstractRecordReader implements EntityReader<T> {

    private final EntityType type;

    private final Method idFieldGetter;

    private final PersistenceContext persistenceContext;

    public RecordEntityReader(PersistenceContext persistenceContext, EntityType type, Class<T> recordClass)
            throws EntityReaderInitialisationException {
        super(type, type.properties(), recordClass);
        this.persistenceContext = persistenceContext;
        var possibleIdFields = Arrays.stream(recordClass.getDeclaredFields())
                .filter(f -> f.getAnnotation(EntityIdField.class) != null)
                .toList();
        if (possibleIdFields.isEmpty()) {
            throw new EntityReaderInitialisationException(
                    "Cannot automatically detect any id field on record type %s and no id field was given"
                            .formatted(recordClass.getName()));
        } else if (possibleIdFields.size() > 1) {
            throw new EntityReaderInitialisationException(
                    "Multiple possible id fields found on record type %s and no id field was given"
                            .formatted(recordClass.getName()));
        }
        var idField = possibleIdFields.get(0).getName();
        try {
            this.type = type;
            this.idFieldGetter = recordClass.getMethod(idField);
        } catch (NoSuchMethodException | SecurityException ex) {
            throw generalInitException(recordClass, idField, ex);
        }
    }

    public RecordEntityReader(
            PersistenceContext persistenceContext, EntityType type, Class<T> recordClass, String idField)
            throws EntityReaderInitialisationException {
        super(type, type.properties(), recordClass);
        this.persistenceContext = persistenceContext;
        try {
            this.type = type;
            this.idFieldGetter = recordClass.getMethod(idField);
        } catch (NoSuchMethodException | SecurityException ex) {
            throw generalInitException(recordClass, idField, ex);
        }
    }

    private static <T extends Record> EntityReaderInitialisationException generalInitException(
            Class<T> recordClass, String idField, Exception ex) {
        return new EntityReaderInitialisationException(
                "Cannot initialize the RecordEntityReader for the record class %s and the idField %s"
                        .formatted(recordClass.getName(), idField),
                ex);
    }

    @Override
    public <K> Entity<K> read(T source) {
        try {
            return this.persistenceContext.newEntity(
                    (K) this.idFieldGetter.invoke(source), type, this.getProperties(source));
        } catch (ReflectiveOperationException | IllegalArgumentException | UnparseableValueException ex) {
            throw new EntityReaderReadException("Error reading the source object %s".formatted(source.toString()), ex);
        }
    }
}
