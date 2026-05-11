package Landing.Backend.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Async // Ejecuta este método sin bloquear la petición HTTP principal
    public void sendWelcomeEmail(String userEmail, String name) {
        System.out.println("📧 [ASYNC] Iniciando envío de correo de bienvenida a: " + userEmail);
        try {
            // Aquí integrarás la API de Resend o Brevo en el futuro
            Thread.sleep(2000); // Simulamos el tiempo de red
            System.out.println("✅ [ASYNC] Correo de bienvenida entregado a: " + name);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Async
    public void sendProjectReadyEmail(String userEmail, String projectName) {
        System.out.println("🚀 [ASYNC] Notificando que el proyecto '" + projectName + "' está listo a: " + userEmail);
        // Lógica de envío de IA lista...
    }
}