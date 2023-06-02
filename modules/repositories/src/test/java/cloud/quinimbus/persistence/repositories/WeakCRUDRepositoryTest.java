package cloud.quinimbus.persistence.repositories;

import cloud.quinimbus.common.annotations.modelling.Owner;
import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.annotation.Entity;
import cloud.quinimbus.persistence.api.annotation.EntityIdField;
import cloud.quinimbus.persistence.api.annotation.EntityTypeClass;
import cloud.quinimbus.persistence.api.annotation.Schema;
import cloud.quinimbus.persistence.api.schema.InvalidSchemaException;
import java.util.Map;
import java.util.ServiceLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WeakCRUDRepositoryTest {

    @Entity(schema = @Schema(id = "crudblog", version = 1L))
    public static record BlogEntry(@EntityIdField String id, String title) {

    }
    
    @Entity(schema = @Schema(id = "crudblog", version = 1L))
    @Owner(owningEntity = BlogEntry.class, field = "entryId")
    public static record BlogEntryComment(@EntityIdField String id, String entryId, String comment, boolean mod) {
        
    }

    @EntityTypeClass(BlogEntry.class)
    public static interface BlogEntryRepository extends CRUDRepository<BlogEntry, String> {

    }

    @EntityTypeClass(BlogEntryComment.class)
    public static interface BlogEntryCommentRepository extends WeakCRUDRepository<BlogEntryComment, String, BlogEntry> {

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
    public void testCreateNewInstanceAndDelete() {
        var blogEntry = new BlogEntry("first", "My first entry");
        this.entryRepository.save(blogEntry);
        this.commentRepository.save(new BlogEntryComment("firstComment", blogEntry.id(), "First!", false));
        
        var secondBlogEntry = new BlogEntry("second", "My second entry");
        this.entryRepository.save(secondBlogEntry);
        
        var findOneResult = this.commentRepository.findOne(blogEntry, "firstComment").orElseThrow();
        assertEquals("First!", findOneResult.comment());
        
        var findOneOnSecondResult = this.commentRepository.findOne(secondBlogEntry, "firstComment");
        assertFalse(findOneOnSecondResult.isPresent());
        
        var findAllResult = this.commentRepository.findAll(blogEntry);
        assertEquals(1, findAllResult.size());
        
        var findAllOnSecondResult = this.commentRepository.findAll(secondBlogEntry);
        assertEquals(0, findAllOnSecondResult.size());

        this.entryRepository.remove("first");
        assertFalse(this.entryRepository.findOne("first").isPresent());
    }
    
    @Test
    public void testFiltering() {
        var blogEntry = new BlogEntry("first", "My first entry");
        this.entryRepository.save(blogEntry);
        this.commentRepository.save(new BlogEntryComment("firstComment", blogEntry.id(), "First!", false));
        this.commentRepository.save(new BlogEntryComment("modComment", blogEntry.id(), "Mod", true));
        var filteredComments = this.commentRepository.findFiltered(blogEntry, Map.of("mod", true));
        assertEquals(1, filteredComments.size());
    }
}
