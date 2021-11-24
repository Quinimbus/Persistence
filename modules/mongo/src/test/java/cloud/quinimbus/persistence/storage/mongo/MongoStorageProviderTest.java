package cloud.quinimbus.persistence.storage.mongo;

import cloud.quinimbus.persistence.test.base.AbstractStorageProviderTest;
import java.util.Map;
import lombok.Getter;

public class MongoStorageProviderTest extends AbstractStorageProviderTest {

    @Getter
    private final MongoPersistenceStorageProvider storageProvider;

    public MongoStorageProviderTest() {
        this.storageProvider = new MongoPersistenceStorageProvider();
    }

    @Override
    public Map<String, Object> getParams() {
        return Map.of(
                "username", "mongoroot",
                "password", "mongorootpassword",
                "host", "localhost");
    }
}
