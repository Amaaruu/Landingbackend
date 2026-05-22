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

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Value("${resend.from-email}")
    private String fromEmailAddress;

    @Value("${support.email}")
    private String supportEmail;

    private void sendEmailViaResend(String toEmail, String subject, String bodyText, String replyToEmail) {
        Resend resend = new Resend(resendApiKey);

        CreateEmailOptions.Builder emailBuilder = CreateEmailOptions.builder()
                .from("WebLandingSuite <" + fromEmailAddress + ">") 
                .to(toEmail)
                .subject(subject)
                .text(bodyText);

        if (replyToEmail != null && !replyToEmail.isBlank()) {
            emailBuilder.replyTo(replyToEmail);
        }

        try {
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
        sendEmailViaResend(supportEmail, subject, text, userEmail);
    }

    @Async
    public void sendWelcomeEmail(String userEmail, String name) {
        System.out.println("📧 [ASYNC] Enviando correo de bienvenida a: " + userEmail);
        String subject = "¡Bienvenido a WebLandingSuite, " + name + "!";
        String text = "Hola " + name + ",\n\n"
                + "¡Gracias por unirte a WebLandingSuite!\n\n"
                + "Saludos,\nEl equipo de WebLandingSuite";
        sendEmailViaResend(userEmail, subject, text, null);
    }

    @Async
    public void sendProjectReadyEmail(String userEmail, String projectName, String signedUrl) {
        System.out.println("📧 [ASYNC] Notificando proyecto listo a: " + userEmail);
        String subject = "🚀 ¡Tu proyecto '" + projectName + "' está listo!";
        String text = "Hola,\n\n"
                + "Tu proyecto: " + projectName + " está listo.\n\n"
                + "Puedes verlo aquí (válido por 24 horas):\n"
                + signedUrl + "\n\n"
                + "Saludos,\nEl equipo de WebLandingSuite";
        sendEmailViaResend(userEmail, subject, text, null);
    }
}