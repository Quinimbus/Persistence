package cloud.quinimbus.persistence.repositories;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.annotation.Entity;
import cloud.quinimbus.persistence.api.annotation.EntityIdField;
import cloud.quinimbus.persistence.api.annotation.EntityTypeClass;
import cloud.quinimbus.persistence.api.annotation.Schema;
import cloud.quinimbus.persistence.api.schema.InvalidSchemaException;
import java.util.Map;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CRUDRepositoryTest {

    @Entity(schema = @Schema(id = "crudblog", version = 1L))
    public static record BlogEntry(@EntityIdField String id, String title) {

    }

    @EntityTypeClass(BlogEntry.class)
    public static interface BlogEntryRepository extends CRUDRepository<BlogEntry, String> {

    }

    @Test
    public void testCreateNewInstance() throws InvalidSchemaException, InvalidRepositoryDefinitionException {
        var ctx = ServiceLoader.load(PersistenceContext.class).findFirst().get();
        ctx.importRecordSchema(BlogEntry.class);
        ctx.setInMemorySchemaStorage("crudblog");

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
    
    @Test
    public void testFiltering() throws InvalidSchemaException, InvalidRepositoryDefinitionException {
        var ctx = ServiceLoader.load(PersistenceContext.class).findFirst().get();
        ctx.importRecordSchema(BlogEntry.class);
        ctx.setInMemorySchemaStorage("crudblog");

        var factory = new RepositoryFactory(ctx);
        var repository = factory.createRepositoryInstance(BlogEntryRepository.class);
        
        repository.save(new BlogEntry("first", "My first entry"));
        repository.save(new BlogEntry("second", "My second entry"));
        var filtered = repository.findFiltered(Map.of("title", "My first entry"));
        assertEquals(1, filtered.size());
        assertEquals("first", filtered.get(0).id());
    }
}
