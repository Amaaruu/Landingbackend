package Landing.Backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import Landing.Backend.dto.LandingProjectRequestDTO;
import Landing.Backend.dto.LandingProjectResponseDTO;
import Landing.Backend.service.LandingProjectService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Proyectos")
public class LandingProjectController {

    private final LandingProjectService projectService;

    // 1. CREAR PROYECTO
    // Tu Service ya maneja la transacción y el mapeo, así que solo llamamos al método.
    @PostMapping
    public ResponseEntity<LandingProjectResponseDTO> createProject(@Valid @RequestBody LandingProjectRequestDTO requestDTO) {
        LandingProjectResponseDTO response = projectService.createProject(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. OBTENER TODOS LOS PROYECTOS
    @GetMapping
    public ResponseEntity<List<LandingProjectResponseDTO>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    // 3. OBTENER UN PROYECTO POR ID
    @GetMapping("/{id}")
    public ResponseEntity<LandingProjectResponseDTO> getProject(@PathVariable Integer id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    // 4. VER LA WEB RENDERIZADA (HTML)
    @GetMapping(value = "/{id}/view", produces = org.springframework.http.MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> viewProjectHtml(@PathVariable Integer id) {
        return ResponseEntity.ok(projectService.getProjectHtml(id));
    }

    // 5. ACTUALIZAR ESTADO
    @PutMapping("/{id}/status")
    public ResponseEntity<LandingProjectResponseDTO> updateStatus(
            @PathVariable Integer id, 
            @RequestParam String status) {
        return ResponseEntity.ok(projectService.updateProjectStatus(id, status));
    }

    // 6. ELIMINAR PROYECTO
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Integer id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}