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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Landing.Backend.dto.LandingProjectRequestDTO;
import Landing.Backend.dto.LandingProjectResponseDTO;
import Landing.Backend.model.LandingProject;
import Landing.Backend.model.Transaction;
import Landing.Backend.service.LandingProjectService;
import Landing.Backend.service.TransactionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class LandingProjectController {

    private final LandingProjectService projectService;
    private final TransactionService transactionService; // Necesario para la relación 1:1

    @PostMapping
    public ResponseEntity<LandingProjectResponseDTO> createProject(@RequestBody LandingProjectRequestDTO requestDTO) {
        
        // Un proyecto solo puede existir si hay una transacción válida que lo respalde
        Transaction transaction = transactionService.getTransactionById(requestDTO.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        LandingProject project = new LandingProject();
        project.setTransaction(transaction);
        project.setProjectName(requestDTO.getProjectName());
        project.setBusinessSector(requestDTO.getBusinessSector());
        project.setCommunicationTone(requestDTO.getCommunicationTone());
        project.setColorPalette(requestDTO.getColorPalette());
        // Status y demás campos se llenan automáticamente en el Service y Entidad

        LandingProject createdProject = projectService.createProject(project);
        return new ResponseEntity<>(convertToResponseDTO(createdProject), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<LandingProjectResponseDTO>> getAllProjects() {
        List<LandingProjectResponseDTO> projects = projectService.getAllProjects().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LandingProjectResponseDTO> getProjectById(@PathVariable Integer id) {
        return projectService.getProjectById(id)
                .map(p -> ResponseEntity.ok(convertToResponseDTO(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<LandingProjectResponseDTO> updateStatus(
            @PathVariable Integer id, 
            @RequestParam String status, 
            @RequestParam(required = false) String url) {
        try {
            LandingProject updatedProject = projectService.updateProjectStatus(id, status, url);
            return ResponseEntity.ok(convertToResponseDTO(updatedProject));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Integer id) {
        try {
            projectService.deleteProject(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- MAPPER ---
    private LandingProjectResponseDTO convertToResponseDTO(LandingProject project) {
        LandingProjectResponseDTO dto = new LandingProjectResponseDTO();
        dto.setProjectId(project.getProjectId());
        dto.setTransactionId(project.getTransaction().getTransactionId());
        dto.setProjectName(project.getProjectName());
        dto.setBusinessSector(project.getBusinessSector());
        dto.setCommunicationTone(project.getCommunicationTone());
        dto.setColorPalette(project.getColorPalette());
        dto.setSignedUrl(project.getSignedUrl());
        dto.setUrlRole(project.getUrlRole());
        dto.setUrlExpiresAt(project.getUrlExpiresAt());
        dto.setAiMetadata(project.getAiMetadata());
        dto.setStatus(project.getStatus());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());
        return dto;
    }
}