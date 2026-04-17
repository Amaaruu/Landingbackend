package Landing.Backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Landing.Backend.model.LandingProject;
import Landing.Backend.service.LandingProjectService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class LandingProjectController {

    private final LandingProjectService projectService;

    //Recibe los datos del Frontend para iniciar un proyecto
    @PostMapping
    public ResponseEntity<LandingProject> createProject(@RequestBody LandingProject project) {
        LandingProject createdProject = projectService.createProject(project);
        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
    }

    //Obtener todos los proyectos (filtrando inactivos automáticamente)
    @GetMapping
    public ResponseEntity<List<LandingProject>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    //Obtener un proyecto por ID
    @GetMapping("/{id}")
    public ResponseEntity<LandingProject> getProjectById(@PathVariable Integer id) {
        return projectService.getProjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //Actualizar el estado del proyecto se actualiza tras llamar a la IA, con opcion de incluir la URL generada
    @PutMapping("/{id}/status")
    public ResponseEntity<LandingProject> updateStatus(
            @PathVariable Integer id, 
            @RequestParam String status, 
            @RequestParam(required = false) String url) {
        try {
            LandingProject updatedProject = projectService.updateProjectStatus(id, status, url);
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