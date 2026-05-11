package Landing.Backend.service;

import Landing.Backend.dto.AiResponseDTO;
import Landing.Backend.dto.LandingProjectRequestDTO;
import Landing.Backend.dto.LandingProjectResponseDTO;
import Landing.Backend.model.LandingProject;
import Landing.Backend.model.Transaction;
import Landing.Backend.repository.LandingProjectRepository;
import Landing.Backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LandingProjectService {

    private final LandingProjectRepository projectRepository;
    private final TransactionRepository transactionRepository;
    private final AiService aiService;
    private final EmailService emailService;

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

    @Transactional
    public String getUserPlanSafe(Integer transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));
        return transaction.getPlan().getName().toUpperCase();
    }

    @Transactional
    public void updateProjectWithAiData(Integer projectId, AiResponseDTO aiResponse) {
        LandingProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        
        project.setAiMetadata(aiResponse.getAiMetadata());
        project.setStatus("Ready");
        projectRepository.save(project);

        // Notificación asíncrona de éxito
        emailService.sendProjectReadyEmail(
            project.getTransaction().getUser().getEmail(), 
            project.getProjectName()
        );
    }

    @Transactional
    public void markProjectAsFailed(Integer projectId) {
        projectRepository.findById(projectId).ifPresent(project -> {
            project.setStatus("Failed");
            projectRepository.save(project);
        });
    }

    public LandingProjectResponseDTO createProject(LandingProjectRequestDTO request) {
        LandingProject project = saveInitialProject(request);
        String userPlan = getUserPlanSafe(request.getTransactionId()); 

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

    // Paginación habilitada para escalabilidad
    public Page<LandingProjectResponseDTO> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable)
                .map(this::mapToResponse);
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

    public LandingProject getProjectEntityById(Integer id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
    }

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