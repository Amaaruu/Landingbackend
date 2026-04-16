package Landing.Backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import Landing.Backend.model.Landing_Project;

@Repository
public interface LandingProjectRepository extends JpaRepository<Landing_Project, Integer> {

    

    
}
