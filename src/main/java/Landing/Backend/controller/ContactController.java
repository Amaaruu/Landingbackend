package Landing.Backend.controller;

import Landing.Backend.dto.ContactRequestDTO;
import Landing.Backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/contact")
public class ContactController {

    @Autowired
    private EmailService emailService;

    @PostMapping
    public ResponseEntity<?> receiveContactForm(@RequestBody ContactRequestDTO request) {
        // Llama al servicio asíncrono
        emailService.sendContactEmail(request.getName(), request.getEmail(), request.getMessage());
        return ResponseEntity.ok(Map.of("message", "Mensaje enviado con éxito"));
    }
}