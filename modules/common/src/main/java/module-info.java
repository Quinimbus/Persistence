module cloud.quinimbus.persistence.common {
    
    exports cloud.quinimbus.persistence.common.filter;
    exports cloud.quinimbus.persistence.common.storage;
    
    requires cloud.quinimbus.persistence.api;
    requires static lombok;
    requires throwing.interfaces;
    requires throwing.streams;
}
