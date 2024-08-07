package cloud.quinimbus.persistence.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface WeakCRUDRepository<T, K, O> {

    K save(T entity);

    Optional<T> findOne(O owner, K id);

    List<T> findAll(O owner);

    List<K> findAllIDs(O owner);

    List<T> findFiltered(O owner, Map<String, Object> properties);

    List<K> findIDsFiltered(O owner, Map<String, Object> properties);

    void remove(O owner, K id);
}
