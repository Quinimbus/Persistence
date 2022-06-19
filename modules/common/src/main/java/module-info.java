module cloud.quinimbus.persistence.common {
    
    exports cloud.quinimbus.persistence.common.filter;
    
    requires cloud.quinimbus.persistence.api;
    requires lombok;
    requires throwing.interfaces;
    requires throwing.streams;
}
