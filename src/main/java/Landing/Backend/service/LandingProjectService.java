package Landing.Backend.service;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import Landing.Backend.model.LandingProject;
import Landing.Backend.repository.LandingProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LandingProjectService {

    private final LandingProjectRepository projectRepository;

    public LandingProject createProject(LandingProject project) {
        // Al crearse, forzamos el estado inicial a 'Processing'
        project.setStatus("Processing");
        return projectRepository.save(project);
    }

    public List<LandingProject> getAllProjects() {
        return projectRepository.findAll();
    }

    public Optional<LandingProject> getProjectById(Integer id) {
        return projectRepository.findById(id);
    }

    //metodo especializado para actualizar el estado tras llamar a la IA
    public LandingProject updateProjectStatus(Integer id, String newStatus, String generatedUrl) {
        return projectRepository.findById(id).map(project -> {
            project.setStatus(newStatus);
            if (generatedUrl != null) {
                project.setSignedUrl(generatedUrl);
            }
            return projectRepository.save(project);
        }).orElseThrow(() -> new RuntimeException("Proyecto no encontrado con ID: " + id));
    }

    //Borrado logico automático gracias a @SQLDelete
    public void deleteProject(Integer id) {
        LandingProject project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con ID: " + id));
        
        projectRepository.delete(project);
    }
}