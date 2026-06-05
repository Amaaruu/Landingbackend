package Landing.Backend.integration;

import Landing.Backend.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DisplayName("ContactController — pruebas de integración")
class ContactControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mvc;

    @Test
    @DisplayName("POST /api/v1/contact → 200 con datos válidos (endpoint público)")
    void shouldAcceptContactForm() throws Exception {
        mvc.perform(post("/api/v1/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name":"Juan Pérez",
                      "email":"juan@test.com",
                      "message":"Necesito información sobre los planes"
                    }
                    """))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.message").value("Mensaje enviado con éxito"));
    }

    @Test
    @DisplayName("POST /api/v1/contact → 200 sin autenticación (endpoint público)")
    void shouldBePublicEndpoint() throws Exception {
        mvc.perform(post("/api/v1/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name":"Test",
                      "email":"test@test.com",
                      "message":"Mensaje de prueba"
                    }
                    """))
           .andExpect(status().isOk());
    }
}