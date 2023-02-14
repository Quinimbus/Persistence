package cloud.quinimbus.persistence.recordmigrationmodel;

import cloud.quinimbus.persistence.api.annotation.Entity;
import cloud.quinimbus.persistence.api.annotation.EntityIdField;
import cloud.quinimbus.persistence.api.annotation.Schema;

public class ModelV1 {

    @Entity(schema = @Schema(id = "blog", version = 1))
    public static record BlogEntry(
            @EntityIdField String id,
            String title) {

    }
}
