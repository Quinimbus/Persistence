package cloud.quinimbus.persistence.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CRUDRepository<T, K> {

    void save(T bundle);

    Optional<T> findOne(K id);

    List<T> findAll();
    
    List<T> findFiltered(Map<String, Object> properties);

    void remove(K id);
}
