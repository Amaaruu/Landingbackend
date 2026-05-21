package Landing.Backend.service;

import Landing.Backend.dto.AiResponseDTO;
import Landing.Backend.model.LandingProject;
import Landing.Backend.repository.LandingProjectRepository;
import Landing.Backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiGenerationTask {

    private final LandingProjectRepository projectRepository;
    private final AiService                aiService;
    private final EmailService             emailService;
    private final JwtService               jwtService;

    @Value("${application.landing.url.base}")
    private String landingBaseUrl;

    @Async
    public void execute(Integer projectId, String userPlan, String userEmail) {
        log.info("[AiGenerationTask] Iniciando generación | projectId={} | plan={} | email={}",
                projectId, userPlan, userEmail);

        try {
            LandingProject project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException(
                            "Proyecto no encontrado con ID: " + projectId));

            AiResponseDTO aiResponse = aiService.requestLandingGeneration(project, userPlan);

            if (aiResponse.getContent() == null || aiResponse.getContent().isEmpty()) {
                throw new RuntimeException(
                        "La API Python retornó contenido vacío para proyecto #" + projectId);
            }

            String token     = jwtService.generateLandingToken(projectId);
            String signedUrl = landingBaseUrl + "/landings/" + projectId + "?token=" + token;

            project.setAiMetadata(aiResponse.getContent());
            project.setSignedUrl(signedUrl);
            project.setUrlExpiresAt(LocalDateTime.now(ZoneId.of("UTC")).plusHours(24));
            project.setStatus("Ready");
            projectRepository.save(project);

            log.info("[AiGenerationTask] Proyecto #{} generado exitosamente. signedUrl={}", 
                    projectId, signedUrl);

            emailService.sendProjectReadyEmail(userEmail, project.getProjectName(), signedUrl);

        } catch (Exception e) {
            log.error("[AiGenerationTask] Error durante la generación del proyecto #{}: {}",
                    projectId, e.getMessage(), e);
            markAsFailed(projectId);
        }
    }

    private void markAsFailed(Integer projectId) {
        projectRepository.findById(projectId).ifPresent(p -> {
            log.warn("[AiGenerationTask] Marcando proyecto #{} como FAILED", projectId);
            p.setStatus("Failed");
            if (p.getTransaction() != null) {
                p.getTransaction().setStatus("FALLIDO");
            }
            projectRepository.save(p);
        });
    }
}