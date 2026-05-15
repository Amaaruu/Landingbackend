// src/main/java/Landing/Backend/controller/LandingProjectController.java
package Landing.Backend.controller;

import Landing.Backend.dto.LandingProjectRequestDTO;
import Landing.Backend.dto.LandingProjectResponseDTO;
import Landing.Backend.service.LandingProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Landing Projects", description = "Gestión de proyectos generados por IA")
public class LandingProjectController {

    private final LandingProjectService projectService;

    @PostMapping
    @Operation(summary = "Crear proyecto", description = "Inicia la generación de una landing page")
    public ResponseEntity<LandingProjectResponseDTO> createProject(
            @RequestBody LandingProjectRequestDTO request) {
        return new ResponseEntity<>(projectService.createProject(request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Listar proyectos", description = "Admin: todos. Usuario: solo los suyos.")
    public ResponseEntity<Page<LandingProjectResponseDTO>> getProjects(
            Pageable pageable,
            Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return ResponseEntity.ok(projectService.getAllProjects(pageable));
        }
        return ResponseEntity.ok(projectService.getProjectsByAuthenticatedUser(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener proyecto por ID")
    public ResponseEntity<LandingProjectResponseDTO> getProjectById(
            @PathVariable Integer id,
            Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return ResponseEntity.ok(projectService.getProjectById(id));
        }
        return ResponseEntity.ok(projectService.getProjectByIdForUser(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar estado del proyecto (admin)")
    public ResponseEntity<LandingProjectResponseDTO> updateProjectStatus(
            @PathVariable Integer id,
            @RequestParam String status) {
        return ResponseEntity.ok(projectService.updateProjectStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar proyecto")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Integer id,
            Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            projectService.deleteProject(id);
        } else {
            projectService.deleteProjectForUser(id);
        }
        return ResponseEntity.noContent().build();
    }
}