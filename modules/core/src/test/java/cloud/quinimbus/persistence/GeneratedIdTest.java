package cloud.quinimbus.persistence;

import cloud.quinimbus.persistence.api.PersistenceContext;
import cloud.quinimbus.persistence.api.PersistenceException;
import cloud.quinimbus.persistence.api.schema.EntityType;
import cloud.quinimbus.persistence.api.schema.EntityTypeBuilder;
import cloud.quinimbus.persistence.api.schema.EntityTypeProperty;
import cloud.quinimbus.persistence.api.schema.EntityTypePropertyBuilder;
import cloud.quinimbus.persistence.api.schema.InvalidSchemaException;
import cloud.quinimbus.persistence.api.schema.Schema;
import cloud.quinimbus.persistence.api.schema.SchemaBuilder;
import cloud.quinimbus.persistence.api.schema.properties.StringPropertyType;
import cloud.quinimbus.persistence.util.FunctionalSchemaProvider;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class GeneratedIdTest {

    private PersistenceContext persistenceContext;

    private Schema schema;

    private EntityType entityType;

    public void initSchema(String idGenerator) throws InvalidSchemaException {
        this.persistenceContext = new PersistenceContextImpl();
        this.entityType = EntityTypeBuilder.builder()
                .id("entity")
                .idGenerator(idGenerator)
                .addProperties(EntityTypePropertyBuilder.builder()
                        .name("title")
                        .type(new StringPropertyType())
                        .structure(EntityTypeProperty.Structure.SINGLE)
                        .build())
                .build();
        this.schema = SchemaBuilder.builder()
                .id("generatedidtest")
                .version(1L)
                .addEntityTypes("entity", this.entityType)
                .build();
        FunctionalSchemaProvider schemaProvider = () -> this.schema;
        this.persistenceContext.importSchema(schemaProvider);
    }

    @Test
    public void testWithoutGenerator() throws PersistenceException, InvalidSchemaException {
        this.initSchema(null);
        var t = assertThrows(
                IllegalArgumentException.class, () -> this.persistenceContext.newEntity(null, this.entityType));
        assertEquals("entity id may not be null", t.getMessage());
    }

    @Test
    public void testWithUUIDGenerator() throws PersistenceException, InvalidSchemaException {
        this.initSchema("uuid");
        var entity = this.persistenceContext.newEntity(null, this.entityType);
        assertNotNull(entity.getId(), "ID should not be null");
        assertTrue(((String) entity.getId()).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
   }

    @Test
    public void testWithFriendlyIDGenerator() throws PersistenceException, InvalidSchemaException {
        this.initSchema("friendly");
        var entity = this.persistenceContext.newEntity(null, this.entityType);
        assertNotNull(entity.getId(), "ID should not be null");
        assertTrue(((String) entity.getId()).matches("[\\w\\d]{1,22}"));
    }
}
