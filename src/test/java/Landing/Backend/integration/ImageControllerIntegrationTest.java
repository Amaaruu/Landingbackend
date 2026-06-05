package Landing.Backend.integration;

import Landing.Backend.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DisplayName("ImageController — pruebas de integración")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ImageControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc      mvc;
    @Autowired private ObjectMapper objectMapper;

    private String userToken;

    @BeforeAll
    void setUp() throws Exception {
        String email = "img_user_" + UUID.randomUUID() + "@test.com";
        mvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Img","lastname":"User","email":"%s","password":"Pass123!"}
                    """.formatted(email)))
           .andExpect(status().isOk());

        MvcResult result = mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"%s","password":"Pass123!"}
                    """.formatted(email)))
           .andExpect(status().isOk()).andReturn();

        userToken = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test @Order(1)
    @DisplayName("POST /api/v1/images/upload → 200 con imagen válida y token JWT")
    void shouldUploadImageSuccessfully() throws Exception {
        when(imageUploadService.uploadImage(any(MultipartFile.class), anyString()))
        .thenReturn("https://res.cloudinary.com/test/image/upload/test.jpg");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        mvc.perform(multipart("/api/v1/images/upload")
                .file(file)
                .param("context", "project")
                .header("Authorization", "Bearer " + userToken))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.imageUrl")
                   .value("https://res.cloudinary.com/test/image/upload/test.jpg"))
           .andExpect(jsonPath("$.message").value("Imagen subida exitosamente"));
    }

    @Test @Order(2)
    @DisplayName("POST /api/v1/images/upload → 403 sin token JWT")
    void shouldReturn403WithoutToken() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        mvc.perform(multipart("/api/v1/images/upload")
                .file(file))
           .andExpect(status().isForbidden());
    }

    @Test @Order(3)
    @DisplayName("POST /api/v1/images/upload → 200 usando context por defecto ('project')")
    void shouldUploadWithDefaultContext() throws Exception {
        when(imageUploadService.uploadImage(any(MultipartFile.class), anyString()))
        .thenReturn("https://res.cloudinary.com/test/image/upload/test.jpg");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "default.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        mvc.perform(multipart("/api/v1/images/upload")
                .file(file)
                .header("Authorization", "Bearer " + userToken))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.imageUrl").isNotEmpty());
    }
}