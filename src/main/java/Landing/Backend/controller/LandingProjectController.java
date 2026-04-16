package Landing.Backend.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import Landing.Backend.model.Landing_Project;
import Landing.Backend.service.LandingProjectService;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class LandingProjectController {

    private final LandingProjectService projectService;

    //Recibe los datos del Frontend para iniciar un proyecto
    @PostMapping
    public ResponseEntity<Landing_Project> createProject(@RequestBody Landing_Project project) {
        Landing_Project createdProject = projectService.createProject(project);
        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
    }

    //Obtener todos los proyectos (filtrando inactivos automáticamente)
    @GetMapping
    public ResponseEntity<List<Landing_Project>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    //Obtener un proyecto por ID
    @GetMapping("/{id}")
    public ResponseEntity<Landing_Project> getProjectById(@PathVariable Integer id) {
        return projectService.getProjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //Actualizar el estado del proyecto se actualiza tras llamar a la IA, con opcion de incluir la URL generada
    @PutMapping("/{id}/status")
    public ResponseEntity<Landing_Project> updateStatus(
            @PathVariable Integer id, 
            @RequestParam String status, 
            @RequestParam(required = false) String url) {
        try {
            Landing_Project updatedProject = projectService.updateProjectStatus(id, status, url);
            return ResponseEntity.ok(updatedProject);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    //Borrado lógico
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Integer id) {
        try {
            projectService.deleteProject(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}