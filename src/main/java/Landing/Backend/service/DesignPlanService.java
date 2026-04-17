package Landing.Backend.service;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import Landing.Backend.model.DesignPlan;
import Landing.Backend.repository.DesignPlanRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DesignPlanService {
    
    private final DesignPlanRepository designPlanRepository;

    public DesignPlan saveDesignPlan(DesignPlan designPlan) {
        return designPlanRepository.save(designPlan);
    }

    // Hibernate filtrará automáticamente y solo traerá los planes con active = true
    public List<DesignPlan> getAllDesignPlans() {
        return designPlanRepository.findAll();
    }

    public Optional<DesignPlan> getPlanById(Integer id) {
        return designPlanRepository.findById(id);
    }

    //Actualizar precio o descripción del plan
    public DesignPlan updatePlan(Integer id, DesignPlan planDetails) {
        return designPlanRepository.findById(id).map(plan -> {
            plan.setName(planDetails.getName());
            plan.setDescription(planDetails.getDescription());
            plan.setPrice(planDetails.getPrice());
            return designPlanRepository.save(plan);
        }).orElseThrow(() -> new RuntimeException("Plan no encontrado con ID: " + id));
    }

    //Borrado lógico automático gracias a @SQLDelete
    public void deletePlan(Integer id) {
        DesignPlan plan = designPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado con ID: " + id));
        
        designPlanRepository.delete(plan);
    }
}