package cloud.quinimbus.persistence.recordmigrationmodel;

import cloud.quinimbus.persistence.api.annotation.Entity;
import cloud.quinimbus.persistence.api.annotation.EntityIdField;
import cloud.quinimbus.persistence.api.annotation.FieldAddMigration;
import cloud.quinimbus.persistence.api.annotation.Schema;

public class ModelV2 {

    public static enum Category {
        UNSORTED, POLITICS, SPORTS
    }

    @Entity(schema = @Schema(id = "blog", version = 2))
    public static record BlogEntry(
            @EntityIdField String id,
            String title,
            @FieldAddMigration(version = 2, value = "UNSORTED") Category category) {

    }
}
