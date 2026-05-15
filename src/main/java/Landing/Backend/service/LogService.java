package Landing.Backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import Landing.Backend.model.Log;
import Landing.Backend.model.LandingProject;
import Landing.Backend.model.User;
import Landing.Backend.repository.LogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LogService {

    private final LogRepository logRepository;

    public Log recordLog(Log logEntry) {
        try {
            Log saved = logRepository.save(logEntry);
            log.info("[LOG AUDIT] Evento '{}' registrado para usuario ID {} | IP: {}",
                    saved.getEventType(),
                    saved.getUser().getUserId(),
                    saved.getIpClient());
            return saved;
        } catch (Exception e) {
            log.error("[LOG AUDIT] Error al registrar evento '{}': {}",
                    logEntry.getEventType(), e.getMessage(), e);
            throw e;
        }
    }

    public void recordEvent(User user, LandingProject project, String eventType, String ipClient) {
        try {
            Log logEntry = Log.builder()
                    .user(user)
                    .project(project)
                    .eventType(eventType)
                    .ipClient(ipClient != null ? ipClient : "unknown")
                    .build();
            logRepository.save(logEntry);
            log.info("[LOG AUDIT] Evento '{}' registrado para usuario '{}' | IP: {}",
                    eventType, user.getEmail(), ipClient);
        } catch (Exception e) {
            log.error("[LOG AUDIT] Fallo silencioso al registrar evento '{}' para '{}': {}",
                    eventType, user.getEmail(), e.getMessage());
        }
    }

    public List<Log> getAllLogs() {
        List<Log> logs = logRepository.findAllByOrderByEventAtDesc();
        log.debug("[LOG AUDIT] Consulta de todos los logs: {} registros encontrados", logs.size());
        return logs;
    }

    public Optional<Log> getLogById(Integer id) {
        return logRepository.findById(id);
    }
}