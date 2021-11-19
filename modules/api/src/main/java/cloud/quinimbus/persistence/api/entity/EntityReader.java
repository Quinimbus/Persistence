package cloud.quinimbus.persistence.api.entity;

public interface EntityReader<T> {

    <K> Entity<K> read(T source);
}
