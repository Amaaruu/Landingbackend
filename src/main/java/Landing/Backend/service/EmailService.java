package Landing.Backend.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    // Estas variables tomarán los valores que pongas en Render (Environment)
    @Value("${resend.api-key}")
    private String resendApiKey;

    @Value("${resend.from-email}") 
    private String fromEmailAddress;

    @Value("${spring.mail.username}") 
    private String supportEmail;

    // Método privado centralizado para enviar a través de la API REST de Resend
    private void sendEmailViaResend(String toEmail, String subject, String bodyText, String replyToEmail) {
        Resend resend = new Resend(resendApiKey);

        // Construimos el correo
        CreateEmailOptions.Builder emailBuilder = CreateEmailOptions.builder()
                .from("weblandingsuite <" + fromEmailAddress + ">")
                .to(toEmail)
                .subject(subject)
                .text(bodyText);

        // Si es el formulario de contacto, agregamos el Reply-To para poder responderle al usuario directamente
        if (replyToEmail != null && !replyToEmail.isBlank()) {
            emailBuilder.replyTo(replyToEmail);
        }

        try {
            // Enviamos a través de la API
            CreateEmailResponse data = resend.emails().send(emailBuilder.build());
            System.out.println("✅ [RESEND] Correo enviado a " + toEmail + ". ID: " + data.getId());
        } catch (ResendException e) {
            System.err.println("❌ [RESEND ERROR] Falló el correo: " + e.getMessage());
        }
    }

    @Async
    public void sendContactEmail(String name, String userEmail, String messageBody) {
        System.out.println("📧 [ASYNC] Procesando correo de contacto de: " + userEmail);
        String subject = "Nuevo mensaje de contacto de: " + name;
        String text = "Nombre: " + name + "\nCorreo: " + userEmail + "\n\nMensaje:\n" + messageBody;
        
        // Enviamos a soporte, poniendo al usuario en el "Reply-To"
        sendEmailViaResend(supportEmail, subject, text, userEmail);
    }

    @Async
    public void sendWelcomeEmail(String userEmail, String name) {
        System.out.println("📧 [ASYNC] Enviando correo de bienvenida a: " + userEmail);
        String subject = "¡Bienvenido a WebLandingSuite, " + name + "!";
        String text = "Hola " + name + ",\n\n"
                + "¡Gracias por unirte a WebLandingSuite! Estamos emocionados de ayudarte a crear Landing Pages increíbles con Inteligencia Artificial.\n\n"
                + "Saludos,\nEl equipo de WebLandingSuite";
                
        sendEmailViaResend(userEmail, subject, text, null);
    }

    @Async
    public void sendProjectReadyEmail(String userEmail, String projectName, String signedUrl) {
        System.out.println("📧 [ASYNC] Notificando proyecto listo a: " + userEmail);
        String subject = "🚀 ¡Tu proyecto '" + projectName + "' está listo!";
        String text = "Hola,\n\n"
                + "Nuestra Inteligencia Artificial ha terminado de generar tu proyecto: " + projectName + ".\n\n"
                + "Puedes verlo aquí (válido por 24 horas):\n"
                + signedUrl + "\n\n"
                + "¡Esperamos que te encante el resultado!\n\n"
                + "Saludos,\nEl equipo de WebLandingSuite";
                
        sendEmailViaResend(userEmail, subject, text, null);
    }
}