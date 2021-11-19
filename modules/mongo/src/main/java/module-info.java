module cloud.quinimbus.persistence.storage.mongo {
    
    uses cloud.quinimbus.persistence.api.PersistenceContext;
    
    exports cloud.quinimbus.persistence.storage.mongo;
    
    requires cloud.quinimbus.persistence.core;
    requires cloud.quinimbus.persistence.api;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;
    requires lombok;
}
