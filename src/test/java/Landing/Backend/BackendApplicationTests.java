package Landing.Backend;

import Landing.Backend.service.AiGenerationTask;
import Landing.Backend.service.AiService;
import Landing.Backend.service.EmailService;
import Landing.Backend.service.ImageUploadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatNoException;

@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {

    @MockBean EmailService      emailService;
    @MockBean AiGenerationTask  aiGenerationTask;
    @MockBean AiService         aiService;
    @MockBean ImageUploadService imageUploadService;

    @Test
    @DisplayName("El contexto de Spring arranca correctamente con el perfil de test")
    void contextLoads() {
        assertThatNoException().isThrownBy(() -> {});
    }
}