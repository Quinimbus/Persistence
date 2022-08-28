open module cloud.quinimbus.persistence.api {
    
    exports cloud.quinimbus.persistence.api;
    exports cloud.quinimbus.persistence.api.annotation;
    exports cloud.quinimbus.persistence.api.entity;
    exports cloud.quinimbus.persistence.api.filter;
    exports cloud.quinimbus.persistence.api.schema;
    exports cloud.quinimbus.persistence.api.schema.properties;
    exports cloud.quinimbus.persistence.api.storage;
    
    requires cloud.quinimbus.config.api;
    requires static lombok;
    requires throwing.streams;
}
