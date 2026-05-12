package Landing.Backend.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Async
    public void sendWelcomeEmail(String userEmail, String name) {
        System.out.println("📧 [ASYNC] Iniciando envío de correo de bienvenida a: " + userEmail);
        try {
            Thread.sleep(2000);
            System.out.println("✅ [ASYNC] Correo de bienvenida entregado a: " + name);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Async
    public void sendProjectReadyEmail(String userEmail, String projectName, String signedUrl) {
        System.out.println("[ASYNC] Notificando que el proyecto '" + projectName + "' está listo a: " + userEmail);
        System.out.println("URL generada (válida 24h): " + signedUrl);
    }
}