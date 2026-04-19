package Landing.Backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Landing.Backend.dto.LogRequestDTO;
import Landing.Backend.dto.LogResponseDTO;
import Landing.Backend.model.LandingProject;
import Landing.Backend.model.Log;
import Landing.Backend.model.User;
import Landing.Backend.service.LandingProjectService;
import Landing.Backend.service.LogService;
import Landing.Backend.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;
    // Inyectamos servicios para resolver las relaciones
    private final UserService userService;
    private final LandingProjectService landingProjectService;

    // POST: Registrar un evento en el sistema
    @PostMapping
    public ResponseEntity<LogResponseDTO> createLog(@RequestBody LogRequestDTO requestDTO) {
        
        // 1. Buscamos al usuario (Obligatorio)
        User user = userService.findById(requestDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado para el log"));

        // 2. Buscamos el proyecto (Opcional, puede ser null)
        LandingProject project = null;
        if (requestDTO.getProjectId() != null) {
            project = landingProjectService.getProjectById(requestDTO.getProjectId())
                    .orElseThrow(() -> new RuntimeException("Proyecto no encontrado para el log"));
        }

        // 3. Ensamblamos la Entidad
        Log log = new Log();
        log.setUser(user);
        log.setProject(project);
        log.setEventType(requestDTO.getEventType());
        log.setIpClient(requestDTO.getIpClient());

        Log createdLog = logService.recordLog(log);
        return new ResponseEntity<>(convertToResponseDTO(createdLog), HttpStatus.CREATED);
    }

    // GET ALL: Leer todo el historial de eventos
    @GetMapping
    public ResponseEntity<List<LogResponseDTO>> getAllLogs() {
        List<LogResponseDTO> logs = logService.getAllLogs().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(logs);
    }

    // GET BY ID: Ver un evento específico
    @GetMapping("/{id}")
    public ResponseEntity<LogResponseDTO> getLogById(@PathVariable Integer id) {
        return logService.getLogById(id)
                .map(log -> ResponseEntity.ok(convertToResponseDTO(log)))
                .orElse(ResponseEntity.notFound().build());
    }

    // --- MAPPER ---
    private LogResponseDTO convertToResponseDTO(Log log) {
        LogResponseDTO dto = new LogResponseDTO();
        dto.setLogId(log.getLogId());
        dto.setUserId(log.getUser().getUserId());
        dto.setUserEmail(log.getUser().getEmail()); // Información útil para auditoría
        
        // Manejamos el caso en que no haya proyecto asociado
        if (log.getProject() != null) {
            dto.setProjectId(log.getProject().getProjectId());
        }
        
        dto.setEventType(log.getEventType());
        dto.setIpClient(log.getIpClient());
        dto.setEventAt(log.getEventAt());
        return dto;
    }
}