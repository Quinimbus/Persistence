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

public class InvalidFilterMethodsTest {

    @Entity(schema = @Schema(id = "crudblog", version = 1L))
    public static record BlogEntry(@EntityIdField String id, String title, String category, boolean active) {

    }
    
    @EntityTypeClass(BlogEntry.class)
    public static interface UnknownPropertyRepository extends CRUDRepository<BlogEntry, String> {
        
        List<BlogEntry> findAllByCategori(String cat);
    }
    
    @EntityTypeClass(BlogEntry.class)
    public static interface AllButOptionalReturnRepository extends CRUDRepository<BlogEntry, String> {
        
        Optional<BlogEntry> findAllByCategory(String cat);
    }
    
    @EntityTypeClass(BlogEntry.class)
    public static interface OneButListReturnRepository extends CRUDRepository<BlogEntry, String> {
        
        List<BlogEntry> findOneByCategory(String cat);
    }
    
    private void createRepository(Class<?> cls) throws InvalidRepositoryDefinitionException, InvalidSchemaException {
        var ctx = ServiceLoader.load(PersistenceContext.class).findFirst().get();
            ctx.importRecordSchema(BlogEntry.class);
            ctx.setInMemorySchemaStorage("crudblog");

            var factory = new RepositoryFactory(ctx);
            factory.createRepositoryInstance(cls);
    }
    
    @Test
    public void testUnknownProperty() {
        assertThrows(
                InvalidRepositoryDefinitionException.class,
                () -> this.createRepository(UnknownPropertyRepository.class));
    }
    
    @Test
    public void testAllButOptionalReturn() {
        assertThrows(
                InvalidRepositoryDefinitionException.class,
                () -> this.createRepository(AllButOptionalReturnRepository.class));
    }
    
    @Test
    public void testOneButListReturn() {
        assertThrows(
                InvalidRepositoryDefinitionException.class,
                () -> this.createRepository(OneButListReturnRepository.class));
    }
}
