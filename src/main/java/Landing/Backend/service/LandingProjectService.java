package Landing.Backend.service;

import Landing.Backend.dto.AiResponseDTO;
import Landing.Backend.dto.LandingProjectRequestDTO;
import Landing.Backend.dto.LandingProjectResponseDTO;
import Landing.Backend.model.LandingProject;
import Landing.Backend.model.Transaction;
import Landing.Backend.repository.LandingProjectRepository;
import Landing.Backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LandingProjectService {

    private final LandingProjectRepository projectRepository;
    private final TransactionRepository transactionRepository;
    private final AiService aiService;

    // Guardado Inicial Rápido
    @Transactional
    public LandingProject saveInitialProject(LandingProjectRequestDTO request) {
        Transaction transaction = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        LandingProject project = LandingProject.builder()
                .transaction(transaction)
                .projectName(request.getProjectName())
                .projectIdea(request.getProjectIdea())
                .callToAction(request.getCallToAction())
                .businessSector(request.getBusinessSector())
                .communicationTone(request.getCommunicationTone())
                .designPreferences(request.getDesignPreferences())
                .status("Processing")
                .build();

        return projectRepository.save(project);
    }

    // Guardado Final Post-IA Rápido
    @Transactional
    public void updateProjectWithAiData(Integer projectId, AiResponseDTO aiResponse) {
        LandingProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        
        project.setAiMetadata(aiResponse.getAiMetadata());
        project.setStatus("Ready");
        projectRepository.save(project);
    }

    @Transactional
    public void markProjectAsFailed(Integer projectId) {
        projectRepository.findById(projectId).ifPresent(project -> {
            project.setStatus("Failed");
            projectRepository.save(project);
        });
    }

    // FLUJO MAESTRO (Asíncrono en BD)
    public LandingProjectResponseDTO createProject(LandingProjectRequestDTO request) {
        
        LandingProject project = saveInitialProject(request);
        String userPlan = project.getTransaction().getPlan().getName().toUpperCase(); 

        try {
            AiResponseDTO aiResponse = aiService.requestLandingGeneration(project, userPlan);
            updateProjectWithAiData(project.getProjectId(), aiResponse);
            return getProjectById(project.getProjectId());
        } catch (Exception e) {
            System.err.println("❌ Error con la IA: " + e.getMessage());
            markProjectAsFailed(project.getProjectId());
            return getProjectById(project.getProjectId());
        }
    }

    public List<LandingProjectResponseDTO> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public LandingProjectResponseDTO getProjectById(Integer id) {
        LandingProject project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        return mapToResponse(project);
    }

    @Transactional
    public LandingProjectResponseDTO updateProjectStatus(Integer id, String status) {
        LandingProject project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        project.setStatus(status);
        projectRepository.save(project);
        return mapToResponse(project);
    }

    @Transactional
    public void deleteProject(Integer id) {
        LandingProject project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        projectRepository.delete(project);
    }

    // MÉTODO REQUERIDO POR LOGCONTROLLER
    public LandingProject getProjectEntityById(Integer id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
    }

    // MAPPER
    private LandingProjectResponseDTO mapToResponse(LandingProject project) {
        return LandingProjectResponseDTO.builder()
                .projectId(project.getProjectId())
                .projectName(project.getProjectName())
                .projectIdea(project.getProjectIdea())
                .callToAction(project.getCallToAction())
                .businessSector(project.getBusinessSector())
                .communicationTone(project.getCommunicationTone())
                .designPreferences(project.getDesignPreferences())
                .status(project.getStatus())
                .signedUrl(project.getSignedUrl())
                .aiMetadata(project.getAiMetadata())
                .createdAt(project.getCreatedAt())
                .build();
    }
}