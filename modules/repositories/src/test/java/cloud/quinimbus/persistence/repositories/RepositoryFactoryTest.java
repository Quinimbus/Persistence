package cloud.quinimbus.persistence.repositories;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.annotation.Entity;
import cloud.quinimbus.persistence.api.annotation.EntityIdField;
import cloud.quinimbus.persistence.api.annotation.EntityTypeClass;
import cloud.quinimbus.persistence.api.annotation.Schema;
import cloud.quinimbus.persistence.api.schema.InvalidSchemaException;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RepositoryFactoryTest {

    @Entity(schema = @Schema(id = "blog", version = 1L))
    public static record BlogEntry(@EntityIdField String id, String title) {

    }
    
    @EntityTypeClass(BlogEntry.class)
    public static interface BlogEntryRepository {

        Optional<BlogEntry> findOne(String id);
        List<BlogEntry> findAll();
        void save(BlogEntry entry);
        void remove(String id);
    }

    @Test
    public void testCreateNewInstance() throws InvalidSchemaException, InvalidRepositoryDefinitionException {
        var ctx = ServiceLoader.load(PersistenceContext.class).findFirst().get();
        ctx.importRecordSchema(BlogEntry.class);
        ctx.setInMemorySchemaStorage("blog");
        
        var factory = new RepositoryFactory(ctx);
        var repository = factory.createRepositoryInstance(BlogEntryRepository.class);
        
        repository.save(new BlogEntry("first", "My first entry"));
        var findOneResult = repository.findOne("first").orElseThrow();
        assertEquals("My first entry", findOneResult.title());
        var findAllResult = repository.findAll();
        assertEquals(1, findAllResult.size());
        
        repository.remove("first");
        assertFalse(repository.findOne("first").isPresent());
    }
}
