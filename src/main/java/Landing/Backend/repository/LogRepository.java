package Landing.Backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Landing.Backend.model.Log;

@Repository
public interface LogRepository extends JpaRepository<Log, Integer> {

    @EntityGraph(attributePaths = {"user", "project"})
    Page<Log> findAllByOrderByEventAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "project"})
    List<Log> findAllByOrderByEventAtDesc();

    @EntityGraph(attributePaths = {"user", "project"})
    Optional<Log> findById(Integer id);
}