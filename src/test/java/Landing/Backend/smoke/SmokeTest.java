package Landing.Backend.smoke;

import Landing.Backend.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DisplayName("Smoke Tests — verificación básica del sistema")
@Tag("regression")
class SmokeTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mvc;

    @Test
    @DisplayName("GET /api/v1/health → 200 OK (servicio vivo)")
    void healthEndpointShouldBeUp() throws Exception {
        mvc.perform(get("/api/v1/health"))
           .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/plans → 200 OK (endpoint público responde)")
    void plansEndpointShouldBeAccessible() throws Exception {
        mvc.perform(get("/api/v1/plans"))
           .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login → no devuelve 500 (servicio de auth operativo)")
    void authServiceShouldNotReturn500() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"notexists@test.com","password":"whatever"}
                    """))
           .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("GET /v3/api-docs → 200 OK (documentación OpenAPI generada)")
    void swaggerApiDocsShouldBeAvailable() throws Exception {
        mvc.perform(get("/v3/api-docs"))
           .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Endpoint inexistente → 4xx, no 500")
    void nonExistentEndpointShouldReturn404() throws Exception {
        mvc.perform(get("/api/v1/nonexistent-endpoint-xyz"))
           .andExpect(status().is4xxClientError());
    }
}