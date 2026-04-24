package Landing.Backend.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import Landing.Backend.dto.DesignPlanRequestDTO;
import Landing.Backend.dto.DesignPlanResponseDTO;
import Landing.Backend.model.DesignPlan;
import Landing.Backend.service.DesignPlanService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class DesignPlanController {

    private final DesignPlanService planService;

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar plan")
    public ResponseEntity<DesignPlanResponseDTO> updatePlan(@PathVariable Integer id, @Valid @RequestBody DesignPlanRequestDTO requestDTO) {
        DesignPlan planDetails = new DesignPlan();
        planDetails.setName(requestDTO.getName());
        planDetails.setDescription(requestDTO.getDescription());
        planDetails.setPrice(requestDTO.getPrice());

        DesignPlan updatedPlan = planService.updatePlan(id, planDetails);
        return ResponseEntity.ok(convertToResponseDTO(updatedPlan));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable Integer id) {
        planService.deletePlan(id);
        return ResponseEntity.noContent().build();
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