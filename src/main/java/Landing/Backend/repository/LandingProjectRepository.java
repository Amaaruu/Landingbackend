package Landing.Backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Landing.Backend.model.LandingProject;

@Repository
public interface LandingProjectRepository extends JpaRepository<LandingProject, Integer> {

}
