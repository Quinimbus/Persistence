package cloud.quinimbus.persistence.storage.mongo;

import cloud.quinimbus.persistence.api.storage.PersistenceStorageProvider;
import cloud.quinimbus.persistence.test.base.AbstractRecordSchemaMigrationTest;
import com.mongodb.client.MongoClients;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;

public class MongoRecordSchemaMigrationTest extends AbstractRecordSchemaMigrationTest {

    @Override
    public PersistenceStorageProvider getStorageProvider() {
        return new MongoPersistenceStorageProvider();
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
