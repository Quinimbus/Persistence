module cloud.quinimbus.persistence.cdi {
    exports cloud.quinimbus.persistence.cdi;

    requires cloud.quinimbus.config.api;
    requires cloud.quinimbus.config.cdi;
    requires cloud.quinimbus.persistence.api;
    requires cloud.quinimbus.persistence.repositories;
    requires jakarta.annotation;
    requires jakarta.cdi;
    requires jakarta.inject;
    requires throwing.streams;
}
