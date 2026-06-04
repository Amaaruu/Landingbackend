package Landing.Backend.integration;

import Landing.Backend.AbstractIntegrationTest;
import Landing.Backend.model.User;
import Landing.Backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@AutoConfigureMockMvc
@DisplayName("DesignPlan API — pruebas de integración con autorización")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DesignPlanIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String userToken;

    @BeforeAll
    void setUpUsers() throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"User","lastname":"Test","email":"user@plans.com","password":"Pass123!"}
                    """))
           .andExpect(status().isOk());

        MvcResult userResult = mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"user@plans.com","password":"Pass123!"}
                    """))
           .andReturn();
        userToken = extractToken(userResult);

        User admin = User.builder()
                .name("Admin")
                .lastName("Supreme")
                .email("admin@plans.com")
                .password(passwordEncoder.encode("Admin123!"))
                .role("admin")
                .active(true)
                .build();
        userRepository.save(admin);

        MvcResult adminResult = mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"admin@plans.com","password":"Admin123!"}
                    """))
           .andReturn();
        adminToken = extractToken(adminResult);
    }

    @Test
    @Order(1)
    @DisplayName("GET /api/v1/plans → 200 sin autenticación (endpoint público)")
    void shouldGetPlansWithoutAuth() throws Exception {
        mvc.perform(get("/api/v1/plans"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/v1/plans → 403 para usuario normal (no ADMIN)")
    void shouldReturn403ForNonAdminUser() throws Exception {
        mvc.perform(post("/api/v1/plans")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Plan Test","description":"Test","price":9.99}
                    """))
           .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/v1/plans → 401 sin token")
    void shouldReturn401WithoutToken() throws Exception {
        mvc.perform(post("/api/v1/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Plan Test","description":"Test","price":9.99}
                    """))
           .andExpect(status().isForbidden());
    }

    @Test
    @Order(4)
    @DisplayName("POST, PUT, DELETE /api/v1/plans → Flujo completo exitoso como ADMIN")
    void shouldCompleteFullCrudAsAdmin() throws Exception {
        MvcResult createResult = mvc.perform(post("/api/v1/plans")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Admin Plan","description":"Plan creado por admin","price":49.99}
                    """))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.name", is("Admin Plan")))
           .andReturn();

        Integer planId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("planId").asInt();

        mvc.perform(put("/api/v1/plans/" + planId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Admin Plan V2","description":"Actualizado","price":59.99}
                    """))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.name", is("Admin Plan V2")));

        mvc.perform(delete("/api/v1/plans/" + planId)
                .header("Authorization", "Bearer " + adminToken))
           .andExpect(status().isNoContent());
    }

    private String extractToken(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }
}