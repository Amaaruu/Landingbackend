package Landing.Backend.service;

import Landing.Backend.dto.AiResponseDTO;
import Landing.Backend.model.LandingProject;
import Landing.Backend.repository.LandingProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AiGenerationTask {

    private final LandingProjectRepository projectRepository;
    private final AiService aiService;
    private final EmailService emailService;

    @Async
    @Transactional
    public void execute(Integer projectId, String userPlan) {
        try {
            LandingProject project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Proyecto no encontrado: " + projectId));

            AiResponseDTO aiResponse = aiService.requestLandingGeneration(project, userPlan);

            project.setAiMetadata(aiResponse.getAiMetadata());
            project.setStatus("Ready");
            projectRepository.save(project);

            emailService.sendProjectReadyEmail(
                project.getTransaction().getUser().getEmail(),
                project.getProjectName()
            );
            
            System.out.println("Proyecto #" + projectId + " completado exitosamente.");

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