import cloud.quinimbus.persistence.api.storage.PersistenceStorageProvider;
import cloud.quinimbus.persistence.storage.mongo.MongoPersistenceStorageProvider;

module cloud.quinimbus.persistence.storage.mongo {
    
    provides PersistenceStorageProvider with MongoPersistenceStorageProvider;
    uses cloud.quinimbus.persistence.api.PersistenceContext;
    exports cloud.quinimbus.persistence.storage.mongo;
    
    requires cloud.quinimbus.common.annotations;
    requires cloud.quinimbus.config.api;
    requires cloud.quinimbus.persistence.core;
    requires cloud.quinimbus.persistence.api;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;
    requires lombok;
}
