package Landing.Backend.service;

import Landing.Backend.dto.LandingProjectRequestDTO;
import Landing.Backend.dto.LandingProjectResponseDTO;
import Landing.Backend.exception.ResourceNotFoundException;
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
    private final AiGenerationTask aiGenerationTask;

    @Transactional
    public LandingProject saveInitialProject(LandingProjectRequestDTO request) {
        Transaction transaction = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transacción no encontrada con ID: " + request.getTransactionId()));

        LandingProject project = LandingProject.builder()
                .transaction(transaction)
                .projectName(request.getProjectName())
                .projectIdea(request.getProjectIdea())
                .callToAction(request.getCallToAction())
                .businessSector(request.getBusinessSector())
                .communicationTone(request.getCommunicationTone())
                .designPreferences(request.getDesignPreferences())
                .landingGoal(request.getLandingGoal())
                .targetAudience(request.getTargetAudience())
                .brandPositioning(request.getBrandPositioning())
                .brandStage(request.getBrandStage())
                .valueProposition(request.getValueProposition())
                .formalityLevel(request.getFormalityLevel())

                .status("Processing")
                .build();

        return projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public String getUserPlanSafe(Integer transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transacción no encontrada con ID: " + transactionId));
        return transaction.getPlan().getName().toUpperCase();
    }

    @Transactional(readOnly = true)
    String getUserEmailSafe(Integer projectId) {
        LandingProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proyecto no encontrado: " + projectId));
        return project.getTransaction().getUser().getEmail();
    }

    public LandingProjectResponseDTO createProject(LandingProjectRequestDTO request) {
        LandingProject project = saveInitialProject(request);
        Integer projectId = project.getProjectId();
        String userPlan   = getUserPlanSafe(request.getTransactionId());
        String userEmail  = getUserEmailSafe(projectId);

        System.out.println("Proyecto #" + projectId + " | Plan: " + userPlan);

        aiGenerationTask.execute(projectId, userPlan, userEmail);

        return getProjectById(projectId);
    }

    public Page<LandingProjectResponseDTO> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable).map(this::mapToResponse);
    }

    public LandingProjectResponseDTO getProjectById(Integer id) {
        LandingProject project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proyecto no encontrado con ID: " + id));
        return mapToResponse(project);
    }

    @Transactional
    public LandingProjectResponseDTO updateProjectStatus(Integer id, String status) {
        LandingProject project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proyecto no encontrado con ID: " + id));
        project.setStatus(status);
        projectRepository.save(project);
        return mapToResponse(project);
    }

    @Transactional
    public void deleteProject(Integer id) {
        LandingProject project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proyecto no encontrado con ID: " + id));
        projectRepository.delete(project);
    }

    public LandingProject getProjectEntityById(Integer id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proyecto no encontrado con ID: " + id));
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
                .landingGoal(project.getLandingGoal())
                .targetAudience(project.getTargetAudience())
                .brandPositioning(project.getBrandPositioning())
                .brandStage(project.getBrandStage())
                .valueProposition(project.getValueProposition())
                .formalityLevel(project.getFormalityLevel())

                .build();
    }
}