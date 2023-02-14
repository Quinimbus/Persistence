package cloud.quinimbus.persistence.storage.mongo;

import cloud.quinimbus.persistence.test.base.AbstractStorageProviderTest;
import com.mongodb.client.MongoClients;
import java.util.Map;
import lombok.Getter;
import org.junit.jupiter.api.AfterEach;

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
    
    @AfterEach
    public void clearDB() {
        var client = MongoClients.create("mongodb://mongoroot:mongorootpassword@localhost");
        var database = client.getDatabase("blog");
        database.drop();
    }
}
