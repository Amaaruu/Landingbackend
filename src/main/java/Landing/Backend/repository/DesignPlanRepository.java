package Landing.Backend.repository;

import Landing.Backend.model.DesignPlan;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface DesignPlanRepository extends JpaRepository<DesignPlan, Integer> {
    
}
