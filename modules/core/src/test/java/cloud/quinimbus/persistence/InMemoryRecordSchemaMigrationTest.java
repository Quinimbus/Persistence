package cloud.quinimbus.persistence;

import cloud.quinimbus.persistence.api.storage.PersistenceStorageProvider;
import cloud.quinimbus.persistence.storage.inmemory.InMemoryPersistenceStorageProvider;
import cloud.quinimbus.persistence.test.base.AbstractRecordSchemaMigrationTest;
import java.util.Map;

public class InMemoryRecordSchemaMigrationTest extends AbstractRecordSchemaMigrationTest {

    @Override
    public PersistenceStorageProvider getStorageProvider() {
        return new InMemoryPersistenceStorageProvider();
    }

    @Override
    public Map<String, Object> getParams() {
        return Map.of();
    }
}
