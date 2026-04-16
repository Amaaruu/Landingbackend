package Landing.Backend.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import Landing.Backend.model.Log;
import Landing.Backend.model.User;

@Repository
public interface LogRepository extends JpaRepository<Log, Integer> {

}
