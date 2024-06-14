package cloud.quinimbus.persistence.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CRUDRepository<T, K> {

    void save(T entity);

    Optional<T> findOne(K id);

    List<T> findAll();

    List<K> findAllIDs();
    
    List<T> findFiltered(Map<String, Object> properties);
    
    List<K> findIDsFiltered(Map<String, Object> properties);

    void remove(K id);
}
