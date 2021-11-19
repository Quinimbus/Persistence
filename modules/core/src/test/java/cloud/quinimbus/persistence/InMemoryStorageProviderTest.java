package cloud.quinimbus.persistence;

import cloud.quinimbus.persistence.storage.inmemory.InMemoryPersistenceStorageProvider;
import cloud.quinimbus.persistence.test.base.AbstractStorageProviderTest;
import lombok.Getter;

public class InMemoryStorageProviderTest extends AbstractStorageProviderTest {
    
    @Getter
    private final InMemoryPersistenceStorageProvider storageProvider;

    public InMemoryStorageProviderTest() {
        this.storageProvider = new InMemoryPersistenceStorageProvider();
    }
}
