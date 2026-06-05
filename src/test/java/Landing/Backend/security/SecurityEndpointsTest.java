package Landing.Backend.security;

import Landing.Backend.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DisplayName("Seguridad — protección de endpoints por rol")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SecurityEndpointsTest extends AbstractIntegrationTest {

    @Autowired private MockMvc      mvc;
    @Autowired private ObjectMapper objectMapper;

    private String regularUserToken;

    @BeforeAll
    void createUserAndGetToken() throws Exception {
        String uniqueEmail = "sec_" + UUID.randomUUID().toString().replace("-", "") + "@test.com";

        mvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name":"Sec",
                      "lastname":"Test",
                      "email":"%s",
                      "password":"Pass123!"
                    }
                    """.formatted(uniqueEmail)))
           .andExpect(status().isOk());

        MvcResult result = mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"%s","password":"Pass123!"}
                    """.formatted(uniqueEmail)))
           .andExpect(status().isOk())
           .andReturn();

        regularUserToken = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    @Test @Order(1)
    @DisplayName("GET /api/v1/users → 403 sin token (requiere ADMIN)")
    void userEndpointRequiresAuth() throws Exception {
        mvc.perform(get("/api/v1/users"))
           .andExpect(status().isForbidden());
    }

    @Test @Order(2)
    @DisplayName("GET /api/v1/logs → 403 sin token (requiere ADMIN)")
    void logsEndpointRequiresAdmin() throws Exception {
        mvc.perform(get("/api/v1/logs"))
           .andExpect(status().isForbidden());
    }

    @Test @Order(3)
    @DisplayName("POST /api/v1/plans → 403 sin token (requiere ADMIN)")
    void createPlanRequiresAdmin() throws Exception {
        mvc.perform(post("/api/v1/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Test","description":"Test","price":9.99}
                    """))
           .andExpect(status().isForbidden());
    }

    @Test @Order(4)
    @DisplayName("GET /api/v1/users → 403 para ROLE_USER (requiere ROLE_ADMIN)")
    void userEndpointForbiddenForRegularUser() throws Exception {
        mvc.perform(get("/api/v1/users")
                .header("Authorization", "Bearer " + regularUserToken))
           .andExpect(status().isForbidden());
    }

    @Test @Order(5)
    @DisplayName("DELETE /api/v1/plans/999 → 403 para ROLE_USER")
    void deletePlanForbiddenForRegularUser() throws Exception {
        mvc.perform(delete("/api/v1/plans/999")
                .header("Authorization", "Bearer " + regularUserToken))
           .andExpect(status().isForbidden());
    }

    @Test @Order(6)
    @DisplayName("GET /api/v1/logs → 403 para ROLE_USER")
    void logsForbiddenForRegularUser() throws Exception {
        mvc.perform(get("/api/v1/logs")
                .header("Authorization", "Bearer " + regularUserToken))
           .andExpect(status().isForbidden());
    }

    @Test @Order(7)
    @DisplayName("Token JWT manipulado → 403 en endpoint protegido")
    void tamperedTokenShouldBeRejected() throws Exception {
        String tamperedToken = regularUserToken + "tampered_suffix";
        mvc.perform(get("/api/v1/transactions/my")
                .header("Authorization", "Bearer " + tamperedToken))
           .andExpect(status().isForbidden());
    }

    @Test @Order(8)
    @DisplayName("Token con formato JWT inválido → 403 en endpoint protegido")
    void invalidTokenFormatShouldBeRejected() throws Exception {
        mvc.perform(get("/api/v1/transactions/my")
                .header("Authorization", "Bearer not.a.valid.jwt.at.all"))
           .andExpect(status().isForbidden());
    }

    @Test @Order(9)
    @DisplayName("POST /auth/login con JSON vacío → 400 (validación @Valid)")
    void emptyLoginBodyShouldReturn400() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
           .andExpect(status().isBadRequest());
    }

    @Test @Order(10)
    @DisplayName("POST /auth/register con password muy corto → 400 (mínimo 8 chars)")
    void shortPasswordShouldReturn400() throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name":"T",
                      "lastname":"U",
                      "email":"short@test.com",
                      "password":"123"
                    }
                    """))
           .andExpect(status().isBadRequest());
    }

    @Test @Order(11)
    @DisplayName("Endpoint inexistente sin token → 403 (Spring Security intercepta antes del dispatcher)")
    void nonExistentEndpointShouldReturn4xx() throws Exception {
        mvc.perform(get("/api/v1/endpoint-que-no-existe"))
           .andExpect(status().isForbidden());
    }
}