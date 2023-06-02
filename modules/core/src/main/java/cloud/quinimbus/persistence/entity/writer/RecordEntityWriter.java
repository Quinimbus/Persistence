package cloud.quinimbus.persistence.entity.writer;

import cloud.quinimbus.persistence.api.entity.Entity;
import cloud.quinimbus.persistence.api.entity.EntityWriter;
import cloud.quinimbus.persistence.api.entity.EntityWriterInitialisationException;
import cloud.quinimbus.persistence.api.schema.EntityType;

public class RecordEntityWriter<T extends Record> extends AbstractRecordWriter<T> implements EntityWriter<T> {

    public RecordEntityWriter(EntityType type, Class<T> recordClass, String idField) throws EntityWriterInitialisationException {
        super(recordClass, type.properties(), idField);
    }

    @Override
    public <K> T write(Entity<K> entity) {
        return super.write(entity);
    }
}
