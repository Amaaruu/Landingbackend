package Landing.Backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
@Tag(name = "Logs de Auditoría", description = "Endpoints para el registro y consulta de eventos del sistema")
public class LogController {

    private final LogService logService;
    private final UserService userService;
    private final LandingProjectService landingProjectService;

    @PostMapping
    @Operation(summary = "Registrar evento manualmente",
               description = "Permite registrar un log de auditoría desde contextos externos")
    public ResponseEntity<LogResponseDTO> createLog(
            @Valid @RequestBody LogRequestDTO requestDTO,
            HttpServletRequest httpRequest) {

        User user = userService.findById(requestDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado para el log"));

        LandingProject project = null;
        if (requestDTO.getProjectId() != null) {
            project = landingProjectService.getProjectEntityById(requestDTO.getProjectId());
        }

        String ipClient = (requestDTO.getIpClient() != null && !requestDTO.getIpClient().isBlank())
                ? requestDTO.getIpClient()
                : extractClientIp(httpRequest);

        Log logEntry = Log.builder()
                .user(user)
                .project(project)
                .eventType(requestDTO.getEventType())
                .ipClient(ipClient)
                .build();

        Log createdLog = logService.recordLog(logEntry);
        log.info("[LOG CONTROLLER] Log creado manualmente: ID {}", createdLog.getLogId());

        return new ResponseEntity<>(convertToResponseDTO(createdLog), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Listar historial de eventos",
               description = "Obtiene todos los logs registrados, ordenados del más reciente al más antiguo")
    public ResponseEntity<List<LogResponseDTO>> getAllLogs() {
        List<LogResponseDTO> logs = logService.getAllLogs().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        log.debug("[LOG CONTROLLER] GET /logs → {} registros", logs.size());
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar evento específico")
    public ResponseEntity<LogResponseDTO> getLogById(@PathVariable Integer id) {
        return logService.getLogById(id)
                .map(logEntry -> ResponseEntity.ok(convertToResponseDTO(logEntry)))
                .orElse(ResponseEntity.notFound().build());
    }

    private LogResponseDTO convertToResponseDTO(Log logEntry) {
        LogResponseDTO dto = new LogResponseDTO();
        dto.setLogId(logEntry.getLogId());
        dto.setUserId(logEntry.getUser().getUserId());
        dto.setUserEmail(logEntry.getUser().getEmail());

        if (logEntry.getProject() != null) {
            dto.setProjectId(logEntry.getProject().getProjectId());
        }

        dto.setEventType(logEntry.getEventType());
        dto.setIpClient(logEntry.getIpClient());
        dto.setEventAt(logEntry.getEventAt());
        return dto;
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}