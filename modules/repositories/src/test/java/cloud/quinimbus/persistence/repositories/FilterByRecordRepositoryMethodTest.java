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
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FilterByRecordRepositoryMethodTest {

    @Entity(schema = @Schema(id = "crudblog", version = 1L))
    public static record BlogEntry(@EntityIdField String id, String title, String category, boolean active) {

    }

    public static record CategoryFilter(String category, boolean active) {

    }
    
    @EntityTypeClass(BlogEntry.class)
    public static interface BlogEntryRepository extends CRUDRepository<BlogEntry, String> {

        List<BlogEntry> findFiltered(CategoryFilter filter);
        
        List<BlogEntry> findByCategory(String category);
        
        Stream<BlogEntry> findAllByCategory(String category);
        
        Optional<BlogEntry> findOneByCategory(String category);
    }
    
    @Test
    public void testFiltering() throws InvalidSchemaException, InvalidRepositoryDefinitionException {
        var ctx = ServiceLoader.load(PersistenceContext.class).findFirst().get();
        ctx.importRecordSchema(BlogEntry.class);
        ctx.setInMemorySchemaStorage("crudblog");

        var factory = new RepositoryFactory(ctx);
        var repository = factory.createRepositoryInstance(BlogEntryRepository.class);
        
        repository.save(new BlogEntry("first", "My first entry", "sports", true));
        repository.save(new BlogEntry("second", "My second entry", "politics", true));
        repository.save(new BlogEntry("third", "My third entry", "sports", false));
        
        var filtered = repository.findFiltered(new CategoryFilter("sports", true));
        assertEquals(1, filtered.size());
        assertEquals("first", filtered.get(0).id());
        
        filtered = repository.findByCategory("politics");
        assertEquals(1, filtered.size());
        assertEquals("second", filtered.get(0).id());
        
        filtered = repository.findAllByCategory("politics").toList();
        assertEquals(1, filtered.size());
        assertEquals("second", filtered.get(0).id());
        
        filtered = repository.findOneByCategory("politics").stream().toList();
        assertEquals(1, filtered.size());
        assertEquals("second", filtered.get(0).id());
    }
}
