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
@Transactional
public class LandingProjectService {

    private final LandingProjectRepository projectRepository;
    private final TransactionRepository transactionRepository;
    private final AiService aiService;

    // --- 1. CREAR ---
    public LandingProjectResponseDTO createProject(LandingProjectRequestDTO request) {
        Transaction transaction = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        LandingProject project = LandingProject.builder()
                .transaction(transaction)
                .projectName(request.getProjectName())
                .businessSector(request.getBusinessSector())
                .communicationTone(request.getCommunicationTone())
                .colorPalette(request.getColorPalette())
                .status("Processing")
                .build();

        project = projectRepository.save(project);

        try {
            AiResponseDTO aiResponse = aiService.requestLandingGeneration(project, "BASIC");
            System.out.println("✅ Código recibido de Python. Motor usado: " + aiResponse.getAi_engine());
            project.setGeneratedHtml(aiResponse.getGeneratedHtml());
            project.setStatus("Ready");
        } catch (Exception e) {
            System.err.println("❌ Error con la IA: " + e.getMessage());
            project.setStatus("Failed");
        }

        projectRepository.save(project);
        return mapToResponse(project);
    }

    // --- 2. LEER TODOS ---
    public List<LandingProjectResponseDTO> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    // --- 3. LEER UNO ---
    public LandingProjectResponseDTO getProjectById(Integer id) {
        LandingProject project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        return mapToResponse(project);
    }

    // --- 4. LEER HTML RENDERIZADO ---
    public String getProjectHtml(Integer id) {
        LandingProject project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        if (project.getGeneratedHtml() == null) {
            return "<html><body><h1>El proyecto aún se está procesando o falló.</h1></body></html>";
        }
        return project.getGeneratedHtml();
    }

    // --- 5. ACTUALIZAR ESTADO ---
    public LandingProjectResponseDTO updateProjectStatus(Integer id, String status) {
        LandingProject project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        project.setStatus(status);
        projectRepository.save(project);
        return mapToResponse(project);
    }

    // --- 6. ELIMINAR ---
    public void deleteProject(Integer id) {
        LandingProject project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        projectRepository.delete(project);
    }

    public LandingProject getProjectEntityById(Integer id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
    }

    // --- MAPPER INTERNO ---
    private LandingProjectResponseDTO mapToResponse(LandingProject project) {
        return LandingProjectResponseDTO.builder()
                .projectId(project.getProjectId())
                .projectName(project.getProjectName())
                .status(project.getStatus())
                .signedUrl(project.getSignedUrl())
                .generatedHtml(project.getGeneratedHtml())
                .createdAt(project.getCreatedAt())
                .build();
    }
}