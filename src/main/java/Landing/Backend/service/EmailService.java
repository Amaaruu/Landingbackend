package Landing.Backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String supportEmail;

    @Async
    public void sendContactEmail(String name, String fromEmail, String messageBody) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            
            mail.setTo(supportEmail); 
            mail.setReplyTo(fromEmail); 
            mail.setSubject("Nuevo mensaje de contacto de: " + name);
            mail.setText("Nombre: " + name + "\nCorreo: " + fromEmail + "\n\nMensaje:\n" + messageBody);
            
            mailSender.send(mail);
            System.out.println("✅ [ASYNC] Correo de contacto recibido de: " + fromEmail);
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Falló el correo de contacto: " + e.getMessage());
        }
    }

    @Async
    public void sendWelcomeEmail(String userEmail, String name) {
        System.out.println("📧 [ASYNC] Enviando correo real de bienvenida a: " + userEmail);
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(userEmail);
            mail.setSubject("¡Bienvenido a WebLandingSuite, " + name + "!");
            mail.setText("Hola " + name + ",\n\n"
                    + "¡Gracias por unirte a WebLandingSuite! Estamos emocionados de ayudarte a crear Landing Pages increíbles con Inteligencia Artificial.\n\n"
                    + "Si tienes alguna duda, responde a este correo.\n\n"
                    + "Saludos,\nEl equipo de WebLandingSuite");
            
            mailSender.send(mail);
            System.out.println("✅ [ASYNC] Correo de bienvenida entregado a: " + name);
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Falló el correo de bienvenida: " + e.getMessage());
        }
    }

    @Async
    public void sendProjectReadyEmail(String userEmail, String projectName, String signedUrl) {
        System.out.println("📧 [ASYNC] Notificando proyecto listo a: " + userEmail);
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(userEmail);
            mail.setSubject("🚀 ¡Tu proyecto '" + projectName + "' está listo!");
            mail.setText("Hola,\n\n"
                    + "Nuestra Inteligencia Artificial ha terminado de generar tu proyecto: " + projectName + ".\n\n"
                    + "Puedes ver el resultado en vivo haciendo clic en el siguiente enlace seguro (válido por 24 horas):\n"
                    + signedUrl + "\n\n"
                    + "¡Esperamos que te encante el resultado!\n\n"
                    + "Saludos,\nEl equipo de WebLandingSuite");
            
            mailSender.send(mail);
            System.out.println("✅ [ASYNC] Correo de proyecto listo enviado a: " + userEmail);
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Falló el correo de proyecto: " + e.getMessage());
        }
    }
}