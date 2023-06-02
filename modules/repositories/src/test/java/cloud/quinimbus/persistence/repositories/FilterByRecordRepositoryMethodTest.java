package cloud.quinimbus.persistence.repositories;

import cloud.quinimbus.common.annotations.modelling.Owner;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FilterByRecordRepositoryMethodTest {

    @Entity(schema = @Schema(id = "crudblog", version = 1L))
    public static record BlogEntry(@EntityIdField String id, String title, String category, boolean active) {

    }

    public static record CategoryFilter(String category, boolean active) {

    }
    
    @Entity(schema = @Schema(id = "crudblog", version = 1L))
    @Owner(owningEntity = BlogEntry.class, field = "entryId")
    public static record BlogEntryComment(@EntityIdField String id, String entryId, String comment, boolean mod) {
        
    }
    
    @EntityTypeClass(BlogEntry.class)
    public static interface BlogEntryRepository extends CRUDRepository<BlogEntry, String> {

        List<BlogEntry> findFiltered(CategoryFilter filter);
        
        List<BlogEntry> findByCategory(String category);
        
        Stream<BlogEntry> findAllByCategory(String category);
        
        Optional<BlogEntry> findOneByCategory(String category);
    }

    @EntityTypeClass(BlogEntryComment.class)
    public static interface BlogEntryCommentRepository extends WeakCRUDRepository<BlogEntryComment, String, BlogEntry> {

        List<BlogEntryComment> findByMod(BlogEntry owner, boolean mod);
    }
    
    private BlogEntryRepository entryRepository;
    
    private BlogEntryCommentRepository commentRepository;

    @BeforeEach
    public void init() throws InvalidSchemaException, InvalidRepositoryDefinitionException {
        var ctx = ServiceLoader.load(PersistenceContext.class).findFirst().get();
        ctx.importRecordSchema(BlogEntry.class, BlogEntryComment.class);
        ctx.setInMemorySchemaStorage("crudblog");

        var factory = new RepositoryFactory(ctx);
        this.entryRepository = factory.createRepositoryInstance(BlogEntryRepository.class);
        this.commentRepository = factory.createRepositoryInstance(BlogEntryCommentRepository.class);
    }
    
    @Test
    public void testFiltering() {
        this.entryRepository.save(new BlogEntry("first", "My first entry", "sports", true));
        this.entryRepository.save(new BlogEntry("second", "My second entry", "politics", true));
        this.entryRepository.save(new BlogEntry("third", "My third entry", "sports", false));
        
        var filtered = this.entryRepository.findFiltered(new CategoryFilter("sports", true));
        assertEquals(1, filtered.size());
        assertEquals("first", filtered.get(0).id());
        
        filtered = this.entryRepository.findByCategory("politics");
        assertEquals(1, filtered.size());
        assertEquals("second", filtered.get(0).id());
        
        filtered = this.entryRepository.findAllByCategory("politics").toList();
        assertEquals(1, filtered.size());
        assertEquals("second", filtered.get(0).id());
        
        filtered = this.entryRepository.findOneByCategory("politics").stream().toList();
        assertEquals(1, filtered.size());
        assertEquals("second", filtered.get(0).id());
    }
    
    @Test
    public void testWeakEntityFiltering() {
        var blogEntry = new BlogEntry("first", "My first entry", "sports", true);
        this.entryRepository.save(blogEntry);
        
        var comment = new BlogEntryComment("firstComment", "first", "First comment", false);
        this.commentRepository.save(comment);
        var modComment = new BlogEntryComment("firstModComment", "first", "First mod comment", true);
        this.commentRepository.save(modComment);
        
        var filtered = this.commentRepository.findByMod(blogEntry, true);
        assertEquals(1, filtered.size());
    }
}
