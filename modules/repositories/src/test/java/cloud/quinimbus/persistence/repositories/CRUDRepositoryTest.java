package cloud.quinimbus.persistence.repositories;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.annotation.Entity;
import cloud.quinimbus.persistence.api.annotation.EntityIdField;
import cloud.quinimbus.persistence.api.annotation.EntityTypeClass;
import cloud.quinimbus.persistence.api.annotation.GenerateID;
import cloud.quinimbus.persistence.api.annotation.Schema;
import cloud.quinimbus.persistence.api.schema.InvalidSchemaException;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CRUDRepositoryTest {

    @Entity(schema = @Schema(id = "crudblog", version = 1L))
    public static record BlogEntry(
            @EntityIdField(generate = @GenerateID(generate = true, generator = "friendly")) String id, String title) {}

    @EntityTypeClass(BlogEntry.class)
    public static interface BlogEntryRepository extends CRUDRepository<BlogEntry, String> {}

    private BlogEntryRepository repository;

    @BeforeEach
    public void init() throws InvalidSchemaException, InvalidRepositoryDefinitionException {
        var ctx = ServiceLoader.load(PersistenceContext.class).findFirst().get();
        ctx.importRecordSchema(BlogEntry.class);
        ctx.setInMemorySchemaStorage("crudblog");

        var factory = new RepositoryFactory(ctx);
        this.repository = factory.createRepositoryInstance(BlogEntryRepository.class);
    }

    @Test
    public void testCreateNewInstance() {
        this.repository.save(new BlogEntry("first", "My first entry"));
        var findOneResult = this.repository.findOne("first").orElseThrow();
        assertEquals("My first entry", findOneResult.title());
        var findAllResult = this.repository.findAll();
        assertEquals(1, findAllResult.size());

        this.repository.remove("first");
        assertFalse(this.repository.findOne("first").isPresent());
    }

    @Test
    public void testCreateNewInstanceWithoutID() {
        var id = this.repository.save(new BlogEntry(null, "My first entry"));
        assertNotNull(id);
        var findOneResult = this.repository.findOne(id).orElseThrow();
        assertEquals("My first entry", findOneResult.title());
        var findAllResult = this.repository.findAll();
        assertEquals(1, findAllResult.size());

        this.repository.remove("first");
        assertFalse(this.repository.findOne("first").isPresent());
    }

    @Test
    public void testFiltering() {
        this.repository.save(new BlogEntry("first", "My first entry"));
        this.repository.save(new BlogEntry("second", "My second entry"));
        var filtered = this.repository.findFiltered(Map.of("title", "My first entry"));
        assertEquals(1, filtered.size());
        assertEquals("first", filtered.get(0).id());
    }

    @Test
    public void testFindIDs() {
        this.repository.save(new BlogEntry("first", "My first entry"));
        this.repository.save(new BlogEntry("second", "My second entry"));
        var ids = this.repository.findAllIDs();
        assertEquals(2, ids.size());
        assertTrue(ids.containsAll(List.of("first", "second")));
        var filtered = this.repository.findIDsFiltered(Map.of("title", "My first entry"));
        assertEquals(1, filtered.size());
        assertEquals("first", filtered.get(0));
    }
}
