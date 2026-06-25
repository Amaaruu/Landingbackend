package Landing.Backend.integration;

import Landing.Backend.AbstractIntegrationTest;
import Landing.Backend.model.Log;
import Landing.Backend.model.User;
import Landing.Backend.repository.LogRepository;
import Landing.Backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DisplayName("LogController — pruebas de integración")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LogControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc         mvc;
    @Autowired private ObjectMapper    objectMapper;
    @Autowired private UserRepository  userRepository;
    @Autowired private LogRepository   logRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String  adminToken;
    private String  userToken;
    private Integer adminUserId;
    private Integer createdLogId;

    @BeforeAll
    void setUp() throws Exception {

        User admin = User.builder()
                .name("LogAdmin").lastName("Test")
                .email("log_admin_" + UUID.randomUUID() + "@test.com")
                .password(passwordEncoder.encode("Admin123!"))
                .role("admin").active(true).build();
        User savedAdmin = userRepository.save(admin);
        adminUserId = savedAdmin.getUserId();

        MvcResult adminResult = mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"%s","password":"Admin123!"}
                    """.formatted(admin.getEmail())))
           .andExpect(status().isOk()).andReturn();
        adminToken = objectMapper.readTree(
                adminResult.getResponse().getContentAsString()).get("token").asText();

        String userEmail = "log_user_" + UUID.randomUUID() + "@test.com";
        mvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Log","lastname":"User","email":"%s","password":"Pass123!"}
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

        Log existingLog = Log.builder()
                .user(savedAdmin)
                .eventType("SYSTEM_TEST")
                .ipClient("127.0.0.1")
                .build();
        Log saved = logRepository.save(existingLog);
        createdLogId = saved.getLogId();
    }

    @Test @Order(1)
    @DisplayName("GET /api/v1/logs → 403 para ROLE_USER")
    void shouldReturn403ForRegularUser() throws Exception {
        mvc.perform(get("/api/v1/logs")
                .header("Authorization", "Bearer " + userToken))
           .andExpect(status().isForbidden());
    }

    @Test @Order(2)
    @DisplayName("GET /api/v1/logs → 403 sin token")
    void shouldReturn403WithoutToken() throws Exception {
        mvc.perform(get("/api/v1/logs"))
           .andExpect(status().isForbidden());
    }

    @Test @Order(3)
    @DisplayName("GET /api/v1/logs → 200 con página de logs (ADMIN)")
    void shouldGetPagedLogs() throws Exception {
        mvc.perform(get("/api/v1/logs")
                .header("Authorization", "Bearer " + adminToken))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.content", isA(java.util.List.class)));
    }

    @Test @Order(4)
    @DisplayName("GET /api/v1/logs/all → 200 con lista completa (ADMIN)")
    void shouldGetAllLogsUnpaged() throws Exception {
        mvc.perform(get("/api/v1/logs/all")
                .header("Authorization", "Bearer " + adminToken))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$", isA(java.util.List.class)))
           .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));
    }

    @Test @Order(5)
    @DisplayName("GET /api/v1/logs/{id} → 200 con datos del log (ADMIN)")
    void shouldGetLogById() throws Exception {
        mvc.perform(get("/api/v1/logs/" + createdLogId)
                .header("Authorization", "Bearer " + adminToken))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.logId", is(createdLogId)))
           .andExpect(jsonPath("$.eventType", is("SYSTEM_TEST")));
    }

    @Test @Order(6)
    @DisplayName("GET /api/v1/logs/{id} → 404 para ID inexistente (ADMIN)")
    void shouldReturn404ForNonExistentLog() throws Exception {
        mvc.perform(get("/api/v1/logs/99999")
                .header("Authorization", "Bearer " + adminToken))
           .andExpect(status().isNotFound());
    }

    @Test @Order(7)
    @DisplayName("POST /api/v1/logs → 201 crea log correctamente (ADMIN)")
    void shouldCreateLog() throws Exception {
        mvc.perform(post("/api/v1/logs")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "userId": %d,
                      "eventType": "MANUAL_TEST_EVENT",
                      "ipClient": "192.168.1.100"
                    }
                    """.formatted(adminUserId)))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.eventType", is("MANUAL_TEST_EVENT")))
           .andExpect(jsonPath("$.ipClient", is("192.168.1.100")));
    }

    @Test @Order(8)
    @DisplayName("POST /api/v1/logs → 400 sin eventType (@NotBlank)")
    void shouldReturn400WithoutEventType() throws Exception {
        mvc.perform(post("/api/v1/logs")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"userId": %d}
                    """.formatted(adminUserId)))
           .andExpect(status().isBadRequest());
    }

    @Test @Order(9)
    @DisplayName("POST /api/v1/logs → 201 con IP extraída del header X-Forwarded-For")
    void shouldCreateLogWithForwardedIp() throws Exception {
        mvc.perform(post("/api/v1/logs")
                .header("Authorization", "Bearer " + adminToken)
                .header("X-Forwarded-For", "203.0.113.42, 10.0.0.1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "userId": %d,
                      "eventType": "FORWARDED_IP_TEST"
                    }
                    """.formatted(adminUserId)))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.ipClient").value("203.0.113.42"));
    }

    @Test @Order(10)
    @DisplayName("POST /api/v1/logs → 201 con projectId null (rama if no ejecutada)")
    void shouldCreateLogWithoutProject() throws Exception {
        mvc.perform(post("/api/v1/logs")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "userId": %d,
                      "eventType": "NO_PROJECT_TEST",
                      "ipClient": "10.0.0.5"
                    }
                    """.formatted(adminUserId)))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.eventType").value("NO_PROJECT_TEST"))
           .andExpect(jsonPath("$.projectId").doesNotExist());
    }
}