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

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
@Tag(name = "Planes de Diseño", description = "Endpoints para gestion de planes de diseño")
public class DesignPlanController {

    private final DesignPlanService planService;

    // POST: Crear un nuevo plan usando el DTO
    @PostMapping
    @Operation(summary = "Crear un nuevo plan de diseño", description = "Crea un nuevo plan de diseño con los datos proporcionados")
    public ResponseEntity<DesignPlanResponseDTO> createPlan(@RequestBody DesignPlanRequestDTO requestDTO) {
        // Mapear de RequestDTO a Entidad
        DesignPlan plan = new DesignPlan();
        plan.setName(requestDTO.getName());
        plan.setDescription(requestDTO.getDescription());
        plan.setPrice(requestDTO.getPrice());

        DesignPlan createdPlan = planService.saveDesignPlan(plan);
        return new ResponseEntity<>(convertToResponseDTO(createdPlan), HttpStatus.CREATED);
    }

    // GET ALL: Obtener el catálogo de planes activos
    @GetMapping
    @Operation(summary = "Obtener todos los planes de diseño", description = "Devuelve una lista de todos los planes de diseño activos registrados")
    public ResponseEntity<List<DesignPlanResponseDTO>> getAllPlans() {
        List<DesignPlanResponseDTO> plans = planService.getAllDesignPlans().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(plans);
    }

    // GET BY ID: Obtener un plan específico
    @GetMapping("/{id}")
    @Operation(summary = "Obtener plan de diseño por ID", description = "Devuelve los detalles de un plan de diseño específico según su ID")
    public ResponseEntity<DesignPlanResponseDTO> getPlanById(@PathVariable Integer id) {
        return planService.getPlanById(id)
                .map(plan -> ResponseEntity.ok(convertToResponseDTO(plan)))
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT: Actualizar información de un plan
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un plan de diseño", description = "Actualiza los datos de un plan de diseño específico según su ID")
    public ResponseEntity<DesignPlanResponseDTO> updatePlan(@PathVariable Integer id, @RequestBody DesignPlanRequestDTO requestDTO) {
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

    // DELETE: Borrado Lógico transparente
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar plan de diseño", description = "Elimina un plan de diseño específico según su ID (borrado lógico)")
    public ResponseEntity<Void> deletePlan(@PathVariable Integer id) {
        try {
            planService.deletePlan(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- MAPPER ---
    private DesignPlanResponseDTO convertToResponseDTO(DesignPlan plan) {
        DesignPlanResponseDTO dto = new DesignPlanResponseDTO();
        dto.setPlanId(plan.getPlanId());
        dto.setName(plan.getName());
        dto.setDescription(plan.getDescription());
        dto.setPrice(plan.getPrice());
        // No exponemos el campo 'active' al Frontend
        return dto;
    }
}