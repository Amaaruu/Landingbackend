package Landing.Backend.service;

import Landing.Backend.dto.AiResponseDTO;
import Landing.Backend.model.LandingProject;
import Landing.Backend.repository.LandingProjectRepository;
import Landing.Backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AiGenerationTask {

    private final LandingProjectRepository projectRepository;
    private final AiService aiService;
    private final EmailService emailService;
    private final JwtService jwtService;

    @Value("${application.landing.url.base}")
    private String landingBaseUrl;

    @Async
    public void execute(Integer projectId, String userPlan, String userEmail) {
        try {
            LandingProject project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Proyecto no encontrado: " + projectId));

            System.out.println("[AiGenerationTask] Iniciando generación | Proyecto #" + projectId + " | Plan: " + userPlan);

            AiResponseDTO aiResponse = aiService.requestLandingGeneration(project, userPlan);

            if (aiResponse.getContent() == null || aiResponse.getContent().isEmpty()) {
                throw new RuntimeException("La API Python retornó contenido vacío para proyecto #" + projectId);
            }

            String token     = jwtService.generateLandingToken(projectId);
            String signedUrl = landingBaseUrl + "/landings/" + projectId + "?token=" + token;

            project.setAiMetadata(aiResponse.getContent());
            project.setSignedUrl(signedUrl);
            project.setUrlExpiresAt(LocalDateTime.now().plusHours(24));
            project.setStatus("Ready");
            projectRepository.save(project);

            emailService.sendProjectReadyEmail(userEmail, project.getProjectName(), signedUrl);

            System.out.println("[AiGenerationTask] ✓ Proyecto #" + projectId + " completado | URL: " + signedUrl);

        } catch (HttpServerErrorException e) {
            System.err.println("[AiGenerationTask] ✗ Error 5xx de Python API | Proyecto #" + projectId);
            System.err.println("  HTTP Status : " + e.getStatusCode());
            System.err.println("  Body        : " + e.getResponseBodyAsString());
            markAsFailed(projectId);

        } catch (HttpClientErrorException e) {
            System.err.println("[AiGenerationTask] ✗ Error 4xx de Python API | Proyecto #" + projectId);
            System.err.println("  HTTP Status : " + e.getStatusCode());
            System.err.println("  Body        : " + e.getResponseBodyAsString());
            markAsFailed(projectId);

        } catch (ResourceAccessException e) {
            System.err.println("[AiGenerationTask] ✗ Timeout o conexión fallida con Python API | Proyecto #" + projectId);
            System.err.println("  Causa: " + e.getMessage());
            markAsFailed(projectId);

        } catch (Exception e) {
            System.err.println("[AiGenerationTask] ✗ Error inesperado | Proyecto #" + projectId
                    + " | " + e.getClass().getSimpleName() + ": " + e.getMessage());
            markAsFailed(projectId);
        }
    }

    private void markAsFailed(Integer projectId) {
        projectRepository.findById(projectId).ifPresent(p -> {
            p.setStatus("Failed");
            projectRepository.save(p);
        });
    }
}