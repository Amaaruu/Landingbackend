package Landing.Backend.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import Landing.Backend.model.LandingProject;
import Landing.Backend.repository.LandingProjectRepository;
import Landing.Backend.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LandingProjectService {

    private final LandingProjectRepository projectRepository;

    public LandingProject createProject(LandingProject project) {
        project.setStatus("Processing");
        return projectRepository.save(project);
    }

    public List<LandingProject> getAllProjects() { 
        return projectRepository.findAll(); 
    }

    public Optional<LandingProject> getProjectById(Integer id) { 
        return projectRepository.findById(id); 
    }

    public LandingProject updateProjectStatus(Integer id, String newStatus, String generatedUrl) {
        return projectRepository.findById(id).map(project -> {
            project.setStatus(newStatus);
            if (generatedUrl != null) {
                project.setSignedUrl(generatedUrl);
            }
            return projectRepository.save(project);
        }).orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con ID: " + id));
    }

    public void deleteProject(Integer id) {
        LandingProject project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con ID: " + id));
        projectRepository.delete(project);
    }
}