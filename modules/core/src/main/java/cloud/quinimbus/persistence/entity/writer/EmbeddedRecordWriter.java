package cloud.quinimbus.persistence.entity.writer;

import cloud.quinimbus.persistence.api.entity.EmbeddedObject;
import cloud.quinimbus.persistence.api.entity.EntityWriterInitialisationException;
import cloud.quinimbus.persistence.api.schema.properties.EmbeddedPropertyType;

public class EmbeddedRecordWriter<T extends Record> extends AbstractRecordWriter<T> {

    public EmbeddedRecordWriter(Class<T> recordClass, EmbeddedPropertyType type) throws EntityWriterInitialisationException {
        super(recordClass, type.properties(), () -> null);
    }

    public T write(EmbeddedObject emb) {
        return super.write(emb);
    }

    public Object tryWrite(Object o) {
        if (o instanceof EmbeddedObject eo) {
            return this.write(eo);
        }
        throw new IllegalArgumentException();
    }
}
