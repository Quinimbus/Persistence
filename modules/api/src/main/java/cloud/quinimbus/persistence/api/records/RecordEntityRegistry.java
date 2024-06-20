package cloud.quinimbus.persistence.api.records;

import java.util.function.Function;

public interface RecordEntityRegistry {

    <T extends Record> String getIdField(Class<T> recordClass);

    <T extends Record, K> Function<T, K> getIdValueGetter(Class<T> entityClass);

    <T extends Record> Class<T> getRecordType(String typeId);
}
