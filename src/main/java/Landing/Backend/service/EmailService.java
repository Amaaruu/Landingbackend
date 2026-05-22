package Landing.Backend.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Value("${resend.from-email}")
    private String fromEmailAddress;

    @Value("${support.email}")
    private String supportEmail;

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── Método base de envío ──────────────────────────────────────────────────
    private void sendEmailViaResend(String toEmail, String subject,
                                    String bodyText, String replyToEmail) {
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
            System.out.println("[RESEND] Correo enviado a " + toEmail
                + " | Asunto: " + subject
                + " | ID: " + data.getId());
        } catch (ResendException e) {
            System.err.println("[RESEND ERROR] Falló correo a " + toEmail
                + " | Asunto: " + subject
                + " | Error: " + e.getMessage());
        }
    }

    // ── Formulario de contacto ────────────────────────────────────────────────
    @Async
    public void sendContactEmail(String name, String userEmail, String messageBody) {
        System.out.println("[ASYNC] Procesando correo de contacto de: " + userEmail);
        String subject = "Nuevo mensaje de contacto de: " + name;
        String text = "Nombre: " + name
                + "\nCorreo: " + userEmail
                + "\n\nMensaje:\n" + messageBody;
        sendEmailViaResend(supportEmail, subject, text, userEmail);
    }

    // ── Bienvenida al usuario + notificación interna ──────────────────────────
    @Async
    public void sendWelcomeEmail(String userEmail, String name) {
        System.out.println("[ASYNC] Enviando correo de bienvenida a: " + userEmail);

        // 1. Email de bienvenida al usuario
        String welcomeSubject = "¡Bienvenido a WebLandingSuite, " + name + "!";
        String welcomeText = "Hola " + name + ",\n\n"
                + "¡Gracias por unirte a WebLandingSuite!\n\n"
                + "Ya puedes acceder a tu cuenta y comenzar a crear "
                + "landing pages profesionales con inteligencia artificial.\n\n"
                + "Visita tu dashboard: https://www.weblandingsuite.com/dashboard\n\n"
                + "Saludos,\nEl equipo de WebLandingSuite";
        sendEmailViaResend(userEmail, welcomeSubject, welcomeText, null);

        // 2. Notificación interna — llega a tu correo
        String internalSubject = "🆕 Nuevo registro: " + name;
        String internalText = "Se registró un nuevo usuario en WebLandingSuite:\n\n"
                + "Nombre: " + name + "\n"
                + "Email: " + userEmail + "\n"
                + "Fecha: " + LocalDateTime.now().format(FORMATTER) + " UTC";
        sendEmailViaResend(supportEmail, internalSubject, internalText, null);
    }

    // ── Proyecto listo al usuario + notificación interna ─────────────────────
    @Async
    public void sendProjectReadyEmail(String userEmail, String projectName,
                                      String signedUrl) {
        System.out.println("📧 [ASYNC] Notificando proyecto listo a: " + userEmail);

        // 1. Email al usuario con su landing
        String userSubject = "🚀 ¡Tu landing page \"" + projectName + "\" está lista!";
        String userText = "Hola,\n\n"
                + "Tu landing page está lista. Puedes verla en el siguiente enlace "
                + "(válido por 24 horas):\n\n"
                + signedUrl + "\n\n"
                + "Recuerda descargar el ZIP desde la página para tenerla de forma permanente.\n\n"
                + "Saludos,\nEl equipo de WebLandingSuite";
        sendEmailViaResend(userEmail, userSubject, userText, null);

        // 2. Notificación interna — llega a tu correo
        String internalSubject = "✅ Landing generada: " + projectName;
        String internalText = "Se generó una nueva landing page:\n\n"
                + "Usuario: " + userEmail + "\n"
                + "Proyecto: " + projectName + "\n"
                + "URL: " + signedUrl + "\n"
                + "Fecha: " + LocalDateTime.now().format(FORMATTER) + " UTC";
        sendEmailViaResend(supportEmail, internalSubject, internalText, null);
    }
}