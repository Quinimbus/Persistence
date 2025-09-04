package cloud.quinimbus.persistence.test.base.recordmigrationmodel;

import cloud.quinimbus.persistence.api.annotation.Embeddable;
import cloud.quinimbus.persistence.api.annotation.Entity;
import cloud.quinimbus.persistence.api.annotation.EntityField;
import cloud.quinimbus.persistence.api.annotation.EntityIdField;
import cloud.quinimbus.persistence.api.annotation.Schema;
import java.util.List;

public class ModelV1 {

    @Embeddable
    public static record Author(String name, Integer rating) {}

    @Entity(schema = @Schema(id = "blog", version = 1))
    public static record BlogEntry(
            @EntityIdField String id,
            String title,
            Author author,
            String type,
            @EntityField(type = String.class) List<String> tags,
            Integer importance) {}
}
