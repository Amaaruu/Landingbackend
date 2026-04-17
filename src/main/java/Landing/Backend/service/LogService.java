package Landing.Backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import Landing.Backend.model.Log;
import Landing.Backend.repository.LogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional
public class LogService {
    
    private final LogRepository logRepository;

    public Log recordLog(Log log) {
        return logRepository.save(log);
    }

    public List<Log> getAllLogs() {
        return logRepository.findAll();
    }

    public Optional<Log> getLogById(Integer id) {
        return logRepository.findById(id);
    }
}