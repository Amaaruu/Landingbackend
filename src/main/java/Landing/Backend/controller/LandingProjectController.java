package Landing.Backend.controller;

import Landing.Backend.dto.LandingProjectRequestDTO;
import Landing.Backend.dto.LandingProjectResponseDTO;
import Landing.Backend.service.LandingProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Landing Projects", description = "Gestión de proyectos generados por IA")
public class LandingProjectController {

    private final LandingProjectService projectService;

    @PostMapping
    public ResponseEntity<LandingProjectResponseDTO> createProject(@RequestBody LandingProjectRequestDTO request) {
        return new ResponseEntity<>(projectService.createProject(request), HttpStatus.CREATED);
    }

    // Adaptado para recibir parámetros de paginación automáticamente
    @GetMapping
    public ResponseEntity<Page<LandingProjectResponseDTO>> getAllProjects(Pageable pageable) {
        return ResponseEntity.ok(projectService.getAllProjects(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LandingProjectResponseDTO> getProjectById(@PathVariable Integer id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<LandingProjectResponseDTO> updateProjectStatus(
            @PathVariable Integer id,
            @RequestParam String status) {
        return ResponseEntity.ok(projectService.updateProjectStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Integer id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}