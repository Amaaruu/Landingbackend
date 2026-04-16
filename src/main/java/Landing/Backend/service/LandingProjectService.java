package Landing.Backend.service;

import org.springframework.stereotype.Service;
import Landing.Backend.model.Landing_Project;
import Landing.Backend.repository.LandingProjectRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LandingProjectService {

    private final LandingProjectRepository projectRepository;

    public Landing_Project createProject(Landing_Project project) {
        // Al crearse, forzamos el estado inicial a 'Processing'
        project.setStatus("Processing");
        return projectRepository.save(project);
    }

    public List<Landing_Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Optional<Landing_Project> getProjectById(Integer id) {
        return projectRepository.findById(id);
    }

    //metodo especializado para actualizar el estado tras llamar a la IA
    public Landing_Project updateProjectStatus(Integer id, String newStatus, String generatedUrl) {
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
        Landing_Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con ID: " + id));
        
        projectRepository.delete(project);
    }
}