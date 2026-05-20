package Landing.Backend.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import Landing.Backend.model.Log;
import Landing.Backend.model.LandingProject;
import Landing.Backend.model.User;
import Landing.Backend.repository.LogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LogService {

    private final LogRepository logRepository;

    public Log recordLog(Log logEntry) {
        return logRepository.save(logEntry);
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
        } catch (Exception ignored) {
        }
    }

    public Page<Log> getAllLogs(Pageable pageable) {
        return logRepository.findAllLogs(pageable);
    }

    public Optional<Log> getLogById(Integer id) {
        return logRepository.findById(id);
    }
}