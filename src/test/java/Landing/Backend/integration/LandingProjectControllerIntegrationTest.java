package Landing.Backend.integration;

import Landing.Backend.AbstractIntegrationTest;
import Landing.Backend.model.*;
import Landing.Backend.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DisplayName("LandingProjectController — pruebas de integración")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LandingProjectControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc               mvc;
    @Autowired private ObjectMapper          objectMapper;
    @Autowired private UserRepository        userRepository;
    @Autowired private DesignPlanRepository  planRepository;
    @Autowired private TransactionRepository txRepository;
    @Autowired private PasswordEncoder       passwordEncoder;

    private String  userToken;
    private String  adminToken;
    private Integer transactionId;
    private Integer createdProjectId;

    private User savedAdmin;

    @BeforeAll
    void setUp() throws Exception {
        DesignPlan plan = DesignPlan.builder()
                .name("LP Plan " + UUID.randomUUID().toString().substring(0, 4))
                .description("Plan para tests de proyectos")
                .price(BigDecimal.valueOf(29.99)).active(true).build();
        planRepository.save(plan);

        String userEmail = "lp_user_" + UUID.randomUUID() + "@test.com";
        mvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"LP","lastname":"User","email":"%s","password":"Pass123!"}
                    """.formatted(userEmail)))
           .andExpect(status().isOk());

        MvcResult userResult = mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"%s","password":"Pass123!"}
                    """.formatted(userEmail)))
           .andExpect(status().isOk()).andReturn();
        userToken = objectMapper.readTree(
                userResult.getResponse().getContentAsString()).get("token").asText();

        User user = userRepository.findByEmail(userEmail).orElseThrow();
        Transaction tx = Transaction.builder()
                .user(user).plan(plan)
                .paymentMethod("transferencia")
                .status("APROBADO").build();
        transactionId = txRepository.save(tx).getTransactionId();

        savedAdmin = User.builder()
                .name("LP").lastName("Admin")
                .email("lp_admin_" + UUID.randomUUID() + "@test.com")
                .password(passwordEncoder.encode("Admin123!"))
                .role("admin").active(true).build();
        savedAdmin = userRepository.save(savedAdmin);

        MvcResult adminResult = mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"%s","password":"Admin123!"}
                    """.formatted(savedAdmin.getEmail())))
           .andExpect(status().isOk()).andReturn();
        adminToken = objectMapper.readTree(
                adminResult.getResponse().getContentAsString()).get("token").asText();
    }

    @Test @Order(1)
    @DisplayName("POST /api/v1/projects → 201 crea proyecto correctamente")
    void shouldCreateProject() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/projects")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "transactionId": %d,
                      "projectName": "Mi Tienda Online",
                      "projectIdea": "Vender café artesanal de especialidad",
                      "callToAction": "Compra ahora",
                      "businessSector": "gastronomia",
                      "landingGoal": "ventas"
                    }
                    """.formatted(transactionId)))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.projectId", notNullValue()))
           .andExpect(jsonPath("$.projectName", is("Mi Tienda Online")))
           .andExpect(jsonPath("$.status", is("Processing")))
           .andReturn();

        createdProjectId = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("projectId").asInt();
    }

    @Test @Order(2)
    @DisplayName("GET /api/v1/projects → 200 usuario ve solo sus proyectos")
    void shouldGetProjectsForUser() throws Exception {
        mvc.perform(get("/api/v1/projects")
                .header("Authorization", "Bearer " + userToken))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.content", isA(java.util.List.class)));
    }

    @Test @Order(3)
    @DisplayName("GET /api/v1/projects → 200 admin ve todos los proyectos")
    void shouldGetAllProjectsForAdmin() throws Exception {
        mvc.perform(get("/api/v1/projects")
                .header("Authorization", "Bearer " + adminToken))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.content", isA(java.util.List.class)));
    }

    @Test @Order(4)
    @DisplayName("GET /api/v1/projects/{id} → 200 usuario accede a su propio proyecto")
    void shouldGetProjectByIdForUser() throws Exception {
        Assumptions.assumeTrue(createdProjectId != null, "Requiere Order(1)");

        mvc.perform(get("/api/v1/projects/" + createdProjectId)
                .header("Authorization", "Bearer " + userToken))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.projectId", is(createdProjectId)));
    }

    @Test @Order(5)
    @DisplayName("GET /api/v1/projects/{id} → 200 admin accede a cualquier proyecto")
    void shouldGetProjectByIdForAdmin() throws Exception {
        Assumptions.assumeTrue(createdProjectId != null, "Requiere Order(1)");

        mvc.perform(get("/api/v1/projects/" + createdProjectId)
                .header("Authorization", "Bearer " + adminToken))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.projectId", is(createdProjectId)));
    }

    @Test @Order(6)
    @DisplayName("PATCH /api/v1/projects/{id}/status → 200 admin actualiza estado")
    void shouldUpdateProjectStatus() throws Exception {
        Assumptions.assumeTrue(createdProjectId != null, "Requiere Order(1)");

        mvc.perform(patch("/api/v1/projects/" + createdProjectId + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .param("status", "Ready"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.status", is("Ready")));
    }

    @Test @Order(7)
    @DisplayName("PATCH /api/v1/projects/{id}/status → 403 para ROLE_USER")
    void shouldReturn403ForUserUpdatingStatus() throws Exception {
        Assumptions.assumeTrue(createdProjectId != null, "Requiere Order(1)");

        mvc.perform(patch("/api/v1/projects/" + createdProjectId + "/status")
                .header("Authorization", "Bearer " + userToken)
                .param("status", "Ready"))
           .andExpect(status().isForbidden());
    }

    @Test @Order(8)
    @DisplayName("DELETE /api/v1/projects/{id} → 204 usuario elimina su propio proyecto")
    void shouldDeleteProjectForUser() throws Exception {
        Assumptions.assumeTrue(createdProjectId != null, "Requiere Order(1)");

        mvc.perform(delete("/api/v1/projects/" + createdProjectId)
                .header("Authorization", "Bearer " + userToken))
           .andExpect(status().isNoContent());
    }

    @Test @Order(9)
    @DisplayName("GET /api/v1/projects → 403 sin token")
    void shouldReturn403WithoutToken() throws Exception {
        mvc.perform(get("/api/v1/projects"))
           .andExpect(status().isForbidden());
    }

    @Test @Order(10)
    @DisplayName("DELETE /api/v1/projects/{id} → 204 admin elimina cualquier proyecto (branch admin)")
    void shouldDeleteProjectAsAdmin() throws Exception {
        DesignPlan plan = planRepository.findAll().stream().findFirst().orElseThrow();

        Transaction adminTx = Transaction.builder()
                .user(savedAdmin)
                .plan(plan)
                .paymentMethod("efectivo")
                .status("APROBADO")
                .build();
        Integer adminTxId = txRepository.save(adminTx).getTransactionId();

        MvcResult result = mvc.perform(post("/api/v1/projects")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "transactionId": %d,
                      "projectName": "Proyecto Admin Delete",
                      "projectIdea": "Idea para eliminar",
                      "callToAction": "Eliminar"
                    }
                    """.formatted(adminTxId)))
           .andExpect(status().isCreated())
           .andReturn();

        Integer projectToDelete = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("projectId").asInt();

        mvc.perform(delete("/api/v1/projects/" + projectToDelete)
                .header("Authorization", "Bearer " + adminToken))
           .andExpect(status().isNoContent());
    }
}