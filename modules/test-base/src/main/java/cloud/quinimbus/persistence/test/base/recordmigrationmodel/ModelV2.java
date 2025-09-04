package cloud.quinimbus.persistence.test.base.recordmigrationmodel;

import cloud.quinimbus.persistence.api.annotation.Embeddable;
import cloud.quinimbus.persistence.api.annotation.Entity;
import cloud.quinimbus.persistence.api.annotation.EntityField;
import cloud.quinimbus.persistence.api.annotation.EntityIdField;
import cloud.quinimbus.persistence.api.annotation.FieldAddMigration;
import cloud.quinimbus.persistence.api.annotation.FieldValueMappingMigration;
import cloud.quinimbus.persistence.api.annotation.Schema;
import java.util.List;

public class ModelV2 {

    public static enum Category {
        UNSORTED,
        POLITICS,
        SPORTS
    }

    public static enum Type {
        NEWS,
        HOWTO,
        TUTORIAL,
        OTHER
    }

    public static enum Importance {
        HIGH,
        MEDIUM,
        LOW
    }

    public static enum AuthorRating {
        BESTSELLER,
        MEDIUM,
        TRASH
    }

    @Embeddable
    public static record Author(
            String name,
            @FieldAddMigration(version = 2, value = "unknown") String subtext,
            @FieldValueMappingMigration(
                            version = 2,
                            value = {
                                @FieldValueMappingMigration.Mapping(oldValue = "1", newValue = "BESTSELLER"),
                                @FieldValueMappingMigration.Mapping(oldValue = "2", newValue = "MEDIUM"),
                                @FieldValueMappingMigration.Mapping(oldValue = "3", newValue = "TRASH")
                            })
                    AuthorRating rating) {}

    @Entity(schema = @Schema(id = "blog", version = 2))
    public static record BlogEntry(
            @EntityIdField String id,
            String title,
            Author author,
            @FieldAddMigration(version = 2, value = "UNSORTED") Category category,
            @FieldValueMappingMigration(
                            version = 2,
                            value = {
                                @FieldValueMappingMigration.Mapping(oldValue = "news", newValue = "NEWS"),
                                @FieldValueMappingMigration.Mapping(
                                        oldValue = "howto",
                                        newValue = "HOWTO",
                                        operator = FieldValueMappingMigration.Operator.EQUALS_IGNORE_CASE),
                                @FieldValueMappingMigration.Mapping(oldValue = "tutorial[s]?", newValue = "TUTORIAL")
                            },
                            ifMissing = FieldValueMappingMigration.MissingMappingOperation.SET_TO_NULL)
                    Type type,
            @EntityField(type = String.class)
                    @FieldValueMappingMigration(
                            version = 2,
                            value = {
                                @FieldValueMappingMigration.Mapping(oldValue = "sports", newValue = "sport"),
                                @FieldValueMappingMigration.Mapping(
                                        oldValue = "tutorial[s]?",
                                        newValue = "workshop",
                                        operator = FieldValueMappingMigration.Operator.REGEX)
                            })
                    List<String> tags,
            @FieldValueMappingMigration(
                            version = 2,
                            value = {
                                @FieldValueMappingMigration.Mapping(oldValue = "1", newValue = "HIGH"),
                                @FieldValueMappingMigration.Mapping(oldValue = "2", newValue = "MEDIUM"),
                                @FieldValueMappingMigration.Mapping(oldValue = "3", newValue = "LOW")
                            })
                    Importance importance) {}
}
