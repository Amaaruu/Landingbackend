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
import Landing.Backend.exception.ResourceNotFoundException;
import Landing.Backend.model.LandingProject;
import Landing.Backend.model.Log;
import Landing.Backend.model.User;
import Landing.Backend.service.LandingProjectService;
import Landing.Backend.service.LogService;
import Landing.Backend.service.UserService;
import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
@Tag(name = "Logs de Auditoría", description = "Endpoints para el registro y consulta de eventos del sistema")
public class LogController {

    private final LogService logService;
    private final UserService userService;
    private final LandingProjectService landingProjectService;

    @PostMapping
    @Operation(summary = "Registrar evento", description = "Almacena un nuevo log de auditoría asegurando la identidad del usuario y la IP")
    public ResponseEntity<LogResponseDTO> createLog(@Valid @RequestBody LogRequestDTO requestDTO) {
        
        User user = userService.findById(requestDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado para el log"));

        LandingProject project = null;
        if (requestDTO.getProjectId() != null) {
            project = landingProjectService.getProjectEntityById(requestDTO.getProjectId());
        }
        
        Log log = new Log();
        log.setUser(user);
        log.setProject(project);
        log.setEventType(requestDTO.getEventType());
        log.setIpClient(requestDTO.getIpClient());

        Log createdLog = logService.recordLog(log);
        return new ResponseEntity<>(convertToResponseDTO(createdLog), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Listar historial de eventos", description = "Obtiene todos los logs registrados en la plataforma para fines de auditoría")
    public ResponseEntity<List<LogResponseDTO>> getAllLogs() {
        List<LogResponseDTO> logs = logService.getAllLogs().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar evento específico", description = "Obtiene los detalles detallados de un log mediante su identificador único")
    public ResponseEntity<LogResponseDTO> getLogById(@PathVariable Integer id) {
        return logService.getLogById(id)
                .map(log -> ResponseEntity.ok(convertToResponseDTO(log)))
                .orElse(ResponseEntity.notFound().build());
    }

    private LogResponseDTO convertToResponseDTO(Log log) {
        LogResponseDTO dto = new LogResponseDTO();
        dto.setLogId(log.getLogId());
        dto.setUserId(log.getUser().getUserId());
        dto.setUserEmail(log.getUser().getEmail()); 
        
        if (log.getProject() != null) {
            dto.setProjectId(log.getProject().getProjectId());
        }
        
        dto.setEventType(log.getEventType());
        dto.setIpClient(log.getIpClient());
        dto.setEventAt(log.getEventAt());
        return dto;
    }
}