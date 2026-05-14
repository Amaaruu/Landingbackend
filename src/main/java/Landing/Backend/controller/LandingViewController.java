// src/main/java/Landing/Backend/controller/LandingViewController.java
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
import java.util.HashMap;
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
            Integer tokenProjectId = jwtService.validateLandingToken(token);

            if (!tokenProjectId.equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Token no corresponde a este proyecto"));
            }

            LandingProject project = projectService.getProjectEntityById(id);

            if (project.getUrlExpiresAt() != null &&
                project.getUrlExpiresAt().isBefore(LocalDateTime.now())) {
                return ResponseEntity.status(HttpStatus.GONE)
                        .body(Map.of("error", "El enlace ha expirado. Genera una nueva landing desde tu cuenta."));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("projectId",         project.getProjectId());
            response.put("projectName",        project.getProjectName());
            response.put("aiMetadata",         project.getAiMetadata());
            response.put("designPreferences",  project.getDesignPreferences());
            response.put("status",             project.getStatus());

            return ResponseEntity.ok(response);

        } catch (BusinessLogicException e) {
            return ResponseEntity.status(e.getStatus())
                    .body(Map.of("error", e.getMessage()));
        }
    }
}