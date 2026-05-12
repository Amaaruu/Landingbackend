package Landing.Backend.controller;

import Landing.Backend.exception.BusinessLogicException;
import Landing.Backend.model.LandingProject;
import Landing.Backend.security.JwtService;
import Landing.Backend.service.LandingProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class LandingViewController {

    private final LandingProjectService projectService;
    private final JwtService jwtService;

    @GetMapping("/landings/{id}")
    public ResponseEntity<?> viewLanding(
            @PathVariable Integer id,
            @RequestParam String token) {
        try {
            // Valida el token y extrae el projectId
            Integer tokenProjectId = jwtService.validateLandingToken(token);

            // Verifica que el token corresponde al proyecto solicitado
            if (!tokenProjectId.equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Token no corresponde a este proyecto"));
            }

            LandingProject project = projectService.getProjectEntityById(id);

            // Doble verificación por fecha en BD
            if (project.getUrlExpiresAt() != null &&
                project.getUrlExpiresAt().isBefore(LocalDateTime.now())) {
                return ResponseEntity.status(HttpStatus.GONE)
                        .body(Map.of("error", "El enlace ha expirado. Genera una nueva landing desde tu cuenta."));
            }

            // Devuelve el aiMetadata con el que el frontend puede renderizar la landing
            return ResponseEntity.ok(Map.of(
                    "projectId",   project.getProjectId(),
                    "projectName", project.getProjectName(),
                    "aiMetadata",  project.getAiMetadata(),
                    "status",      project.getStatus()
            ));

        } catch (BusinessLogicException e) {
            return ResponseEntity.status(e.getStatus())
                    .body(Map.of("error", e.getMessage()));
        }
    }
}