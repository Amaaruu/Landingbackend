package Landing.Backend.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import Landing.Backend.model.Landing_Project;
import Landing.Backend.repository.LandingProjectRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LandingProjectService {

    private final LandingProjectRepository ProjectRepository;

    public Landing_Project createProject(Landing_Project project) {
        return ProjectRepository.save(project);
    }

    public Optional<Landing_Project> getProjectById(Integer id) {
        return ProjectRepository.findById(id);
    }

    public void deleteProject(Integer id) {
        ProjectRepository.deleteById(id);
    }
    
}
