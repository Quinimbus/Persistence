module cloud.quinimbus.persistence.cdi {
    
    exports cloud.quinimbus.persistence.cdi;
    
    requires cloud.quinimbus.config.api;
    requires cloud.quinimbus.config.cdi;
    requires cloud.quinimbus.persistence.api;
    requires cloud.quinimbus.persistence.repositories;
    requires jakarta.enterprise.cdi.api;
    requires java.annotation;
    requires jakarta.inject.api;
}
