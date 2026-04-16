package Landing.Backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import Landing.Backend.model.Log;
import Landing.Backend.repository.LogRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogService {
    private final LogRepository logRepository;

    public Log recordLog(Log log) {
        return logRepository.save(log);
    }

    public List<Log> getAllLogs() {
        return logRepository.findAll();
    }
}
