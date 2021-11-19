package cloud.quinimbus.persistence.api.schema;

import cloud.quinimbus.persistence.api.schema.properties.BooleanPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.EmbeddedPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.EnumPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.IntegerPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.StringPropertyType;
import cloud.quinimbus.persistence.api.schema.properties.TimestampPropertyType;

public sealed interface EntityTypePropertyType permits BooleanPropertyType, EmbeddedPropertyType, EnumPropertyType, IntegerPropertyType, StringPropertyType, TimestampPropertyType {

}
