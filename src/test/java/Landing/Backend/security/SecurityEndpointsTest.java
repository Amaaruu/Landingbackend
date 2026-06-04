package Landing.Backend.security;

import Landing.Backend.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DisplayName("Seguridad — protección de endpoints por rol")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SecurityEndpointsTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

    private static String regularUserToken;

    @BeforeAll
    void createUserAndGetToken() throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Sec","lastname":"Test","email":"sec@test.com","password":"Pass123!"}
                    """))
           .andExpect(status().isOk()); // Verificamos que el registro sea exitoso

        MvcResult result = mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"sec@test.com","password":"Pass123!"}
                    """))
           .andExpect(status().isOk())
           .andReturn();
           
        regularUserToken = objectMapper.readTree(
                result.getResponse().getContentAsString())
                .get("token").asText();
    }

    @Test @Order(1)
    @DisplayName("GET /api/v1/users → 4xx sin token")
    void userEndpointRequiresAuth() throws Exception {
        mvc.perform(get("/api/v1/users"))
           .andExpect(status().is4xxClientError());
    }

    @Test @Order(2)
    @DisplayName("GET /api/v1/logs → 4xx sin token")
    void logsEndpointRequiresAdmin() throws Exception {
        mvc.perform(get("/api/v1/logs"))
           .andExpect(status().is4xxClientError());
    }

    @Test @Order(3)
    @DisplayName("POST /api/v1/plans → 4xx sin token (solo ADMIN)")
    void createPlanRequiresAdmin() throws Exception {
        mvc.perform(post("/api/v1/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Test","description":"Test","price":9.99}
                    """))
           .andExpect(status().is4xxClientError());
    }

    @Test @Order(4)
    @DisplayName("GET /api/v1/users → 403 para ROLE_USER (requiere ADMIN)")
    void userEndpointForbiddenForRegularUser() throws Exception {
        mvc.perform(get("/api/v1/users")
                .header("Authorization", "Bearer " + regularUserToken))
           .andExpect(status().isForbidden());
    }

    @Test @Order(5)
    @DisplayName("DELETE /api/v1/plans/1 → 403 para ROLE_USER")
    void deletePlanForbiddenForRegularUser() throws Exception {
        mvc.perform(delete("/api/v1/plans/1")
                .header("Authorization", "Bearer " + regularUserToken))
           .andExpect(status().isForbidden());
    }

    @Test @Order(6)
    @DisplayName("Token JWT manipulado → 4xx en endpoint protegido")
    void tamperedTokenShouldBeRejected() throws Exception {
        String tamperedToken = regularUserToken + "tampered";
        mvc.perform(get("/api/v1/transactions/my")
                .header("Authorization", "Bearer " + tamperedToken))
           .andExpect(status().is4xxClientError());
    }

    @Test @Order(7)
    @DisplayName("Token con formato inválido → 4xx en endpoint protegido")
    void invalidTokenFormatShouldBeRejected() throws Exception {
        mvc.perform(get("/api/v1/transactions/my")
                .header("Authorization", "Bearer not.a.valid.jwt"))
           .andExpect(status().is4xxClientError());
    }

    @Test @Order(8)
    @DisplayName("POST /auth/login con payload vacío → 400 (no 500)")
    void emptyLoginBodyShouldReturn400() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
           .andExpect(status().isBadRequest());
    }

    @Test @Order(9)
    @DisplayName("POST /auth/register con password muy corto → 400")
    void shortPasswordShouldReturn400() throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"T","lastname":"U","email":"t@t.com","password":"123"}
                    """))
           .andExpect(status().isBadRequest());
    }
}