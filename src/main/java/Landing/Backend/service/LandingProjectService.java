// src/main/java/Landing/Backend/service/LandingProjectService.java
package Landing.Backend.service;

import Landing.Backend.dto.LandingProjectRequestDTO;
import Landing.Backend.dto.LandingProjectResponseDTO;
import Landing.Backend.exception.BusinessLogicException;
import Landing.Backend.exception.ResourceNotFoundException;
import Landing.Backend.model.LandingProject;
import Landing.Backend.model.Transaction;
import Landing.Backend.model.User;
import Landing.Backend.repository.LandingProjectRepository;
import Landing.Backend.repository.TransactionRepository;
import Landing.Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LandingProjectService {

    private final LandingProjectRepository projectRepository;
    private final TransactionRepository    transactionRepository;
    private final UserRepository           userRepository;
    private final AiGenerationTask         aiGenerationTask;

    private String getAuthenticatedEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessLogicException("No autenticado", HttpStatus.UNAUTHORIZED);
        }
        return auth.getName();
    }

    private User getAuthenticatedUser() {
        String email = getAuthenticatedEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessLogicException("Usuario autenticado no encontrado", HttpStatus.UNAUTHORIZED));
    }

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

    public LandingProjectResponseDTO createProject(LandingProjectRequestDTO request) {
        LandingProject project  = saveInitialProject(request);
        Integer        projectId = project.getProjectId();
        String         userPlan  = getUserPlanSafe(request.getTransactionId());
        String         userEmail = getUserEmailSafe(projectId);

        System.out.println("Proyecto #" + projectId + " | Plan: " + userPlan);

        aiGenerationTask.execute(projectId, userPlan, userEmail);

        return getProjectById(projectId);
    }

    public Page<LandingProjectResponseDTO> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable).map(this::mapToResponse);
    }

    public Page<LandingProjectResponseDTO> getProjectsByAuthenticatedUser(Pageable pageable) {
        User user = getAuthenticatedUser();
        return projectRepository.findByUserId(user.getUserId(), pageable)
                .map(this::mapToResponse);
    }

    public LandingProjectResponseDTO getProjectByIdForUser(Integer id) {
        User user = getAuthenticatedUser();
        LandingProject project = projectRepository
                .findByProjectIdAndUserId(id, user.getUserId())
                .orElseThrow(() -> {
                    boolean exists = projectRepository.existsById(id);
                    if (exists) {
                        return new BusinessLogicException(
                                "No tienes permiso para acceder a este proyecto", HttpStatus.FORBIDDEN);
                    }
                    return new ResourceNotFoundException("Proyecto no encontrado con ID: " + id);
                });
        return mapToResponse(project);
    }

    public LandingProjectResponseDTO getProjectById(Integer id) {
        LandingProject project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proyecto no encontrado con ID: " + id));
        return mapToResponse(project);
    }

    public LandingProject getProjectEntityById(Integer id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proyecto no encontrado con ID: " + id));
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

    @Transactional
    public void deleteProjectForUser(Integer id) {
        User user = getAuthenticatedUser();
        LandingProject project = projectRepository
                .findByProjectIdAndUserId(id, user.getUserId())
                .orElseThrow(() -> {
                    boolean exists = projectRepository.existsById(id);
                    if (exists) {
                        return new BusinessLogicException(
                                "No tienes permiso para eliminar este proyecto", HttpStatus.FORBIDDEN);
                    }
                    return new ResourceNotFoundException("Proyecto no encontrado con ID: " + id);
                });
        projectRepository.delete(project);
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