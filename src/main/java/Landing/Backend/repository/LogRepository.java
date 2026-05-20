package Landing.Backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import Landing.Backend.model.Log;

@Repository
public interface LogRepository extends JpaRepository<Log, Integer> {

    @Query(value = "SELECT l FROM Log l JOIN FETCH l.user ORDER BY l.eventAt DESC", countQuery = "SELECT COUNT(l) FROM Log l")
    Page<Log> findAllLogs(Pageable pageable);
}