package cloud.quinimbus.persistence.storage.mongo;

import cloud.quinimbus.persistence.test.base.AbstractStorageProviderTest;
import lombok.Getter;

public class MongoStorageProviderTest extends AbstractStorageProviderTest {
    
    @Getter
    private final MongoPersistenceStorageProvider storageProvider;

    public MongoStorageProviderTest() {
        this.storageProvider = new MongoPersistenceStorageProvider("localhost", "mongoroot", "mongorootpassword");
    }
}
