package Landing.Backend.service;

import Landing.Backend.dto.AiResponseDTO;
import Landing.Backend.model.LandingProject;
import Landing.Backend.repository.LandingProjectRepository;
import Landing.Backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

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

            AiResponseDTO aiResponse = aiService.requestLandingGeneration(project, userPlan);

            // Generar token firmado con expiración de 24h
            String token = jwtService.generateLandingToken(projectId);
            String signedUrl = landingBaseUrl + "/landings/" + projectId + "?token=" + token;

            project.setAiMetadata(aiResponse.getAiMetadata());
            project.setSignedUrl(signedUrl);
            project.setUrlExpiresAt(LocalDateTime.now().plusHours(24));
            project.setStatus("Ready");
            projectRepository.save(project);

            emailService.sendProjectReadyEmail(userEmail, project.getProjectName(), signedUrl);

            System.out.println("✅ Proyecto #" + projectId + " completado | URL: " + signedUrl);

        } catch (Exception e) {
            System.err.println("Error en proyecto #" + projectId
                    + " | " + e.getClass().getSimpleName()
                    + ": " + e.getMessage());

            projectRepository.findById(projectId).ifPresent(p -> {
                p.setStatus("Failed");
                projectRepository.save(p);
            });
        }
    }
}