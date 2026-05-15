package Landing.Backend.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import Landing.Backend.model.Log;

import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<Log, Integer> {

    // Retorna logs ordenados del más reciente al más antiguo
    List<Log> findAllByOrderByEventAtDesc();

    // Útil para auditoría filtrada por usuario
    List<Log> findByUserUserIdOrderByEventAtDesc(Integer userId);
}