module cloud.quinimbus.persistence.repositories {
    
    uses cloud.quinimbus.persistence.api.PersistenceContext;
    
    opens cloud.quinimbus.persistence.repositories to cloud.quinimbus.persistence.core;
    
    exports cloud.quinimbus.persistence.repositories;
    
    requires cloud.quinimbus.common.tools;
    requires cloud.quinimbus.persistence.api;
    requires cloud.quinimbus.persistence.common;
    requires cloud.quinimbus.tools;
    requires lombok;
    requires throwing.interfaces;
    requires throwing.streams;
}
