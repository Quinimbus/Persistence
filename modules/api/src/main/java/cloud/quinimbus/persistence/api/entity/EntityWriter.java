package cloud.quinimbus.persistence.api.entity;

public interface EntityWriter<T> {

    <K> T write(Entity<K> entity);
}
