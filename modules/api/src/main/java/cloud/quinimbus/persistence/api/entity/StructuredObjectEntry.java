package cloud.quinimbus.persistence.api.entity;

public interface StructuredObjectEntry<T extends StructuredObjectEntryType> {
    
    String key();
    
    Object value();
    
    T type();
    
    boolean partial();
}
