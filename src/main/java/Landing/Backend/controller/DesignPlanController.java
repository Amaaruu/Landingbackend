package Landing.Backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@Tag(name = "Planes de Diseño", description = "Gestión del catálogo de planes y precios")
public class DesignPlanController {

    private final DesignPlanService planService;

    @PostMapping
    @Operation(summary = "Crear nuevo plan", description = "Añade un plan de diseño al catálogo comercial")
    public ResponseEntity<DesignPlanResponseDTO> createPlan(@Valid @RequestBody DesignPlanRequestDTO requestDTO) {
        DesignPlan plan = new DesignPlan();
        plan.setName(requestDTO.getName());
        plan.setDescription(requestDTO.getDescription());
        plan.setPrice(requestDTO.getPrice());

        DesignPlan createdPlan = planService.saveDesignPlan(plan);
        return new ResponseEntity<>(convertToResponseDTO(createdPlan), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Listar planes activos", description = "Recupera todos los planes disponibles para la venta")
    public ResponseEntity<List<DesignPlanResponseDTO>> getAllPlans() {
        List<DesignPlanResponseDTO> plans = planService.getAllDesignPlans().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar plan", description = "Obtiene la información detallada de un plan específico")
    public ResponseEntity<DesignPlanResponseDTO> getPlanById(@PathVariable Integer id) {
        return planService.getPlanById(id)
                .map(plan -> ResponseEntity.ok(convertToResponseDTO(plan)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar plan", description = "Modifica precios o descripciones de un plan existente")
    public ResponseEntity<DesignPlanResponseDTO> updatePlan(@PathVariable Integer id, @Valid @RequestBody DesignPlanRequestDTO requestDTO) {
        try {
            DesignPlan planDetails = new DesignPlan();
            planDetails.setName(requestDTO.getName());
            planDetails.setDescription(requestDTO.getDescription());
            planDetails.setPrice(requestDTO.getPrice());

            DesignPlan updatedPlan = planService.updatePlan(id, planDetails);
            return ResponseEntity.ok(convertToResponseDTO(updatedPlan));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar plan", description = "Realiza un borrado lógico del plan del catálogo")
    public ResponseEntity<Void> deletePlan(@PathVariable Integer id) {
        try {
            planService.deletePlan(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private DesignPlanResponseDTO convertToResponseDTO(DesignPlan plan) {
        DesignPlanResponseDTO dto = new DesignPlanResponseDTO();
        dto.setPlanId(plan.getPlanId());
        dto.setName(plan.getName());
        dto.setDescription(plan.getDescription());
        dto.setPrice(plan.getPrice());
        return dto;
    }
}