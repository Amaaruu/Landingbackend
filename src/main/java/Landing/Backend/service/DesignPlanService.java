package Landing.Backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import Landing.Backend.model.DesignPlan;
import Landing.Backend.repository.DesignPlanRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DesignPlanService {
    
    private final DesignPlanRepository designPlanRepository;

    public List<DesignPlan> getAllDesignPlans() {
        return designPlanRepository.findAll();
    }

    public DesignPlan saveDesignPlan(DesignPlan designPlan) {
        return designPlanRepository.save(designPlan);
    }
}
