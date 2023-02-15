package cloud.quinimbus.persistence.recordmigrationmodel;

import cloud.quinimbus.persistence.api.annotation.Embeddable;
import cloud.quinimbus.persistence.api.annotation.Entity;
import cloud.quinimbus.persistence.api.annotation.EntityIdField;
import cloud.quinimbus.persistence.api.annotation.Schema;

public class ModelV1 {
    
    @Embeddable
    public static record Author(String name) {
        
    }

    @Entity(schema = @Schema(id = "blog", version = 1))
    public static record BlogEntry(
            @EntityIdField String id,
            String title,
            Author author) {

    }
}
