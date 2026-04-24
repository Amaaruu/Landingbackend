package Landing.Backend.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import Landing.Backend.dto.LandingProjectRequestDTO;
import Landing.Backend.dto.LandingProjectResponseDTO;
import Landing.Backend.model.LandingProject;
import Landing.Backend.model.Transaction;
import Landing.Backend.service.LandingProjectService;
import Landing.Backend.service.TransactionService;
import Landing.Backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Proyectos")
public class LandingProjectController {

    private final LandingProjectService projectService;
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<LandingProjectResponseDTO> createProject(@Valid @RequestBody LandingProjectRequestDTO requestDTO) {
        // Lanzamos 404 si la transacción vinculada no existe
        Transaction transaction = transactionService.getTransactionById(requestDTO.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("Transacción no encontrada con ID: " + requestDTO.getTransactionId()));

        LandingProject project = new LandingProject();
        project.setTransaction(transaction);
        project.setProjectName(requestDTO.getProjectName());
        project.setBusinessSector(requestDTO.getBusinessSector());
        project.setCommunicationTone(requestDTO.getCommunicationTone());
        project.setColorPalette(requestDTO.getColorPalette());

        LandingProject createdProject = projectService.createProject(project);
        return new ResponseEntity<>(convertToResponseDTO(createdProject), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<LandingProjectResponseDTO> updateStatus(
            @PathVariable Integer id, 
            @RequestParam String status, 
            @RequestParam(required = false) String url) {
        LandingProject updatedProject = projectService.updateProjectStatus(id, status, url);
        return ResponseEntity.ok(convertToResponseDTO(updatedProject));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Integer id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    private LandingProjectResponseDTO convertToResponseDTO(LandingProject project) {
        LandingProjectResponseDTO dto = new LandingProjectResponseDTO();
        dto.setProjectId(project.getProjectId());
        dto.setTransactionId(project.getTransaction().getTransactionId());
        dto.setProjectName(project.getProjectName());
        dto.setBusinessSector(project.getBusinessSector());
        dto.setCommunicationTone(project.getCommunicationTone());
        dto.setColorPalette(project.getColorPalette());
        dto.setSignedUrl(project.getSignedUrl());
        dto.setStatus(project.getStatus());
        dto.setAiMetadata(project.getAiMetadata());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());
        return dto;
    }
}