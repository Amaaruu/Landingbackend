package Landing.Backend.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import Landing.Backend.model.Log;
import Landing.Backend.service.LogService;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    //Registrar un evento en el sistema
    @PostMapping
    public ResponseEntity<Log> createLog(@RequestBody Log log) {
        Log createdLog = logService.recordLog(log);
        return new ResponseEntity<>(createdLog, HttpStatus.CREATED);
    }

    //Leer todo el historial de eventos
    @GetMapping
    public ResponseEntity<List<Log>> getAllLogs() {
        return ResponseEntity.ok(logService.getAllLogs());
    }

    //Ver un evento específico
    @GetMapping("/{id}")
    public ResponseEntity<Log> getLogById(@PathVariable Integer id) {
        return logService.getLogById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}