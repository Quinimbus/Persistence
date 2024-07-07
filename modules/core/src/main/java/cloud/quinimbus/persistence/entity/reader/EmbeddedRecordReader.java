package cloud.quinimbus.persistence.entity.reader;

import cloud.quinimbus.persistence.api.entity.EmbeddedObject;
import cloud.quinimbus.persistence.api.entity.EntityReaderInitialisationException;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.properties.EmbeddedPropertyType;
import cloud.quinimbus.persistence.entity.DefaultEmbeddedObject;
import cloud.quinimbus.persistence.exception.EntityReaderReadException;
import java.util.List;

public class EmbeddedRecordReader<T extends Record> extends AbstractRecordReader {

    private final EmbeddedPropertyType type;

    private final String[] path;

    private final EntityType parentType;

    public EmbeddedRecordReader(
            EmbeddedPropertyType type, Class<T> recordClass, List<String> path, EntityType parentType)
            throws EntityReaderInitialisationException {
        super(parentType, type.properties(), recordClass);
        this.path = path.toArray(new String[] {});
        this.parentType = parentType;
        this.type = type;
    }

    public EmbeddedObject readRecord(T source) {
        try {
            return new DefaultEmbeddedObject(
                    this.path, this.parentType, this.getProperties(source), this.getTransientFields(source), this.type);
        } catch (ReflectiveOperationException ex) {
            throw new EntityReaderReadException("Error reading the source object %s".formatted(source.toString()), ex);
        }
    }

    public EmbeddedObject tryRead(Object o) {
        if (o instanceof Record r) {
            return this.readRecord((T) r);
        }
        throw new IllegalArgumentException();
    }
}
