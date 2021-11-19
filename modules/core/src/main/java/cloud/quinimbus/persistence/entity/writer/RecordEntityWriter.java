package cloud.quinimbus.persistence.entity.writer;

import cloud.quinimbus.persistence.api.annotation.EntityIdField;
import cloud.quinimbus.persistence.api.entity.Entity;
import cloud.quinimbus.persistence.api.entity.EntityWriter;
import cloud.quinimbus.persistence.api.entity.EntityWriterInitialisationException;
import cloud.quinimbus.persistence.api.schema.EntityType;
import java.util.Arrays;

public class RecordEntityWriter<T extends Record> extends AbstractRecordWriter<T> implements EntityWriter<T> {

    public RecordEntityWriter(EntityType type, Class<T> recordClass) throws EntityWriterInitialisationException {
        super(recordClass, type.properties(), () -> {
            var possibleIdFields = Arrays.stream(recordClass.getDeclaredFields())
                    .filter(f -> f.getAnnotation(EntityIdField.class) != null)
                    .toList();
            if (possibleIdFields.isEmpty()) {
                throw new EntityWriterInitialisationException("Cannot automatically detect any id field on record type %s and no id field was given"
                        .formatted(recordClass.getName()));
            } else if (possibleIdFields.size() > 1) {
                throw new EntityWriterInitialisationException("Multiple possible id fields found on record type %s and no id field was given"
                        .formatted(recordClass.getName()));
            }
            return possibleIdFields.get(0).getName();
        });
    }

    public RecordEntityWriter(EntityType type, Class<T> recordClass, String idField) throws EntityWriterInitialisationException {
        super(recordClass, type.properties(), () -> idField);
    }

    @Override
    public <K> T write(Entity<K> entity) {
        return super.write(entity);
    }
}
