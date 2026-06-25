package Landing.Backend.integration;

import Landing.Backend.AbstractIntegrationTest;
import Landing.Backend.model.DesignPlan;
import Landing.Backend.model.User;
import Landing.Backend.repository.DesignPlanRepository;
import Landing.Backend.repository.UserRepository;
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
@DisplayName("TransactionController — pruebas de integración")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransactionIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc             mvc;
    @Autowired private ObjectMapper        objectMapper;
    @Autowired private UserRepository      userRepository;
    @Autowired private DesignPlanRepository planRepository;
    @Autowired private PasswordEncoder     passwordEncoder;

    private String userToken;
    private String adminToken;
    private Integer planId;
    private Integer createdTransactionId;

    @BeforeAll
    void setUp() throws Exception {
        DesignPlan plan = DesignPlan.builder()
                .name("Plan TX " + UUID.randomUUID().toString().substring(0, 6))
                .description("Plan para tests de transacciones")
                .price(BigDecimal.valueOf(29.99))
                .active(true).build();
        planId = planRepository.save(plan).getPlanId();

        String userEmail = "tx_user_" + UUID.randomUUID() + "@test.com";
        mvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"TX","lastname":"User","email":"%s","password":"Pass123!"}
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

        User admin = User.builder()
                .name("Admin").lastName("TX")
                .email("admin_tx_" + UUID.randomUUID() + "@test.com")
                .password(passwordEncoder.encode("Admin123!"))
                .role("admin").active(true).build();
        userRepository.save(admin);

        MvcResult adminResult = mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"%s","password":"Admin123!"}
                    """.formatted(admin.getEmail())))
           .andExpect(status().isOk()).andReturn();
        adminToken = objectMapper.readTree(
                adminResult.getResponse().getContentAsString()).get("token").asText();
    }

    @Test @Order(1)
    @DisplayName("POST /api/v1/transactions → 201 crea transacción correctamente")
    void shouldCreateTransaction() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/transactions")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"planId":%d,"paymentMethod":"transferencia"}
                    """.formatted(planId)))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.transactionId", notNullValue()))
           .andExpect(jsonPath("$.status", is("PENDIENTE")))
           .andExpect(jsonPath("$.paymentMethod", is("transferencia")))
           .andReturn();

        createdTransactionId = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("transactionId").asInt();
    }

    @Test @Order(2)
    @DisplayName("POST /api/v1/transactions → 400 sin paymentMethod (@NotBlank)")
    void shouldReturn400WithoutPaymentMethod() throws Exception {
        mvc.perform(post("/api/v1/transactions")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"planId":%d}
                    """.formatted(planId)))
           .andExpect(status().isBadRequest());
    }

    @Test @Order(3)
    @DisplayName("GET /api/v1/transactions/my → 200 lista transacciones del usuario")
    void shouldGetMyTransactions() throws Exception {
        mvc.perform(get("/api/v1/transactions/my")
                .header("Authorization", "Bearer " + userToken))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$", isA(java.util.List.class)))
           .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));
    }

    @Test @Order(4)
    @DisplayName("GET /api/v1/transactions/my → 403 sin token")
    void shouldReturn403WithoutToken() throws Exception {
        mvc.perform(get("/api/v1/transactions/my"))
           .andExpect(status().isForbidden());
    }

    @Test @Order(5)
    @DisplayName("PUT /api/v1/transactions/{id}/status → 200 actualiza estado (ADMIN)")
    void shouldUpdateTransactionStatus() throws Exception {
        Assumptions.assumeTrue(createdTransactionId != null,
                "Requiere que el test Order(1) haya creado una transacción");

        mvc.perform(put("/api/v1/transactions/" + createdTransactionId + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .param("status", "APROBADO"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.status", is("APROBADO")));
    }

    @Test @Order(6)
    @DisplayName("PUT /api/v1/transactions/{id}/status → 403 para ROLE_USER")
    void shouldReturn403ForUserUpdatingStatus() throws Exception {
        mvc.perform(put("/api/v1/transactions/1/status")
                .header("Authorization", "Bearer " + userToken)
                .param("status", "APROBADO"))
           .andExpect(status().isForbidden());
    }

    @Test @Order(7)
    @DisplayName("GET /api/v1/transactions/user/{userId} → 403 para ROLE_USER")
    void shouldReturn403ForUserAccessingOtherUserTransactions() throws Exception {
        mvc.perform(get("/api/v1/transactions/user/1")
                .header("Authorization", "Bearer " + userToken))
           .andExpect(status().isForbidden());
    }
}