package Landing.Backend;

import Landing.Backend.service.AiGenerationTask;
import Landing.Backend.service.AiService;
import Landing.Backend.service.EmailService;
import Landing.Backend.service.ImageUploadService;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractIntegrationTest {

    // ── Servicios externos — mockeados para evitar llamadas reales ──────────
    @MockBean
    protected EmailService emailService;

    @MockBean
    protected AiGenerationTask aiGenerationTask;

    @MockBean
    protected AiService aiService;

    @MockBean
    protected ImageUploadService imageUploadService;
}