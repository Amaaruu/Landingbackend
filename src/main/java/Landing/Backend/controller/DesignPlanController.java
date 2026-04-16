package Landing.Backend.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import Landing.Backend.model.DesignPlan;
import Landing.Backend.service.DesignPlanService;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class DesignPlanController {

    private final DesignPlanService planService;

    //Crear un nuevo plan
    @PostMapping
    public ResponseEntity<DesignPlan> createPlan(@RequestBody DesignPlan plan) {
        DesignPlan createdPlan = planService.saveDesignPlan(plan);
        return new ResponseEntity<>(createdPlan, HttpStatus.CREATED);
    }

    //Obtener el catálogo de planes activos
    @GetMapping
    public ResponseEntity<List<DesignPlan>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllDesignPlans());
    }

    //Obtener un plan específico
    @GetMapping("/{id}")
    public ResponseEntity<DesignPlan> getPlanById(@PathVariable Integer id) {
        return planService.getPlanById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //Actualizar información de un plan
    @PutMapping("/{id}")
    public ResponseEntity<DesignPlan> updatePlan(@PathVariable Integer id, @RequestBody DesignPlan plan) {
        try {
            DesignPlan updatedPlan = planService.updatePlan(id, plan);
            return ResponseEntity.ok(updatedPlan);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    //Borrado Lógico transparente
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable Integer id) {
        try {
            planService.deletePlan(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}