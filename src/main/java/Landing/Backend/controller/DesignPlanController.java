package Landing.Backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import Landing.Backend.dto.DesignPlanRequestDTO;
import Landing.Backend.dto.DesignPlanResponseDTO;
import Landing.Backend.model.DesignPlan;
import Landing.Backend.service.DesignPlanService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
@Tag(name = "Planes de Diseño")
public class DesignPlanController {

    private final DesignPlanService designPlanService; // Usamos un solo nombre consistente

    @PostMapping
    @Operation(summary = "Crear un nuevo plan de diseño")
    public ResponseEntity<DesignPlanResponseDTO> createPlan(@Valid @RequestBody DesignPlanRequestDTO requestDTO) {
        DesignPlan plan = new DesignPlan();
        plan.setName(requestDTO.getName());
        plan.setDescription(requestDTO.getDescription());
        plan.setPrice(requestDTO.getPrice());
        
        DesignPlan savedPlan = designPlanService.saveDesignPlan(plan);
        return ResponseEntity.status(201).body(convertToResponseDTO(savedPlan));
    }

    @GetMapping
    @Operation(summary = "Listar todos los planes")
    public ResponseEntity<List<DesignPlanResponseDTO>> getAllPlans() {
        List<DesignPlanResponseDTO> plans = designPlanService.getAllDesignPlans()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(plans);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar plan")
    public ResponseEntity<DesignPlanResponseDTO> updatePlan(@PathVariable Integer id, @Valid @RequestBody DesignPlanRequestDTO requestDTO) {
        DesignPlan planDetails = new DesignPlan();
        planDetails.setName(requestDTO.getName());
        planDetails.setDescription(requestDTO.getDescription());
        planDetails.setPrice(requestDTO.getPrice());

        DesignPlan updatedPlan = designPlanService.updatePlan(id, planDetails);
        return ResponseEntity.ok(convertToResponseDTO(updatedPlan));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar plan")
    public ResponseEntity<Void> deletePlan(@PathVariable Integer id) {
        designPlanService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }

    // UN SOLO MÉTODO de conversión al final para evitar el error de duplicado
    private DesignPlanResponseDTO convertToResponseDTO(DesignPlan plan) {
        DesignPlanResponseDTO dto = new DesignPlanResponseDTO();
        dto.setPlanId(plan.getPlanId());
        dto.setName(plan.getName());
        dto.setDescription(plan.getDescription());
        dto.setPrice(plan.getPrice());
        return dto;
    }
}