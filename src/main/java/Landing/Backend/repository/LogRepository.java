package Landing.Backend.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import Landing.Backend.model.Log;

import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<Log, Integer> {

    /**
     * JOIN FETCH explícito sobre el usuario.
     * Evita el lazy load individual que dispara @SQLRestriction("active = true")
     * en la entidad User. Con JOIN FETCH, Hibernate resuelve la relación en una
     * sola query SQL directa desde la tabla log, sin filtrar por active en users.
     */
    @Query("SELECT l FROM Log l JOIN FETCH l.user ORDER BY l.eventAt DESC")
    List<Log> findAllByOrderByEventAtDesc();

    @Query("SELECT l FROM Log l JOIN FETCH l.user WHERE l.user.userId = :userId ORDER BY l.eventAt DESC")
    List<Log> findByUserUserIdOrderByEventAtDesc(Integer userId);
}