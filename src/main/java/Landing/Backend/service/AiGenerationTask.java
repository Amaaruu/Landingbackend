package Landing.Backend.service;

import Landing.Backend.dto.AiResponseDTO;
import Landing.Backend.model.LandingProject;
import Landing.Backend.repository.LandingProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiGenerationTask {

    private final LandingProjectRepository projectRepository;
    private final AiService aiService;
    private final LandingProjectService landingProjectService;

    @Async
    public void execute(Integer projectId, String userPlan) {
        try {
            LandingProject project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Proyecto no encontrado: " + projectId));

            AiResponseDTO aiResponse = aiService.requestLandingGeneration(project, userPlan);

            landingProjectService.updateProjectWithAiData(projectId, aiResponse);
            System.out.println("✅ Proyecto #" + projectId + " completado exitosamente.");

        } catch (Exception e) {
            System.err.println("Error en proyecto #" + projectId
                    + " | " + e.getClass().getSimpleName()
                    + ": " + e.getMessage());
            landingProjectService.markProjectAsFailed(projectId);
        }
    }
}