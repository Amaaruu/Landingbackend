package Landing.Backend.integration;

import Landing.Backend.AbstractIntegrationTest;
import Landing.Backend.model.*;
import Landing.Backend.repository.*;
import Landing.Backend.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DisplayName("LandingViewController — pruebas de integración")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LandingViewControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc                  mvc;
    @Autowired private UserRepository           userRepository;
    @Autowired private DesignPlanRepository     planRepository;
    @Autowired private TransactionRepository    txRepository;
    @Autowired private LandingProjectRepository projectRepository;
    @Autowired private JwtService               jwtService;
    @Autowired private PasswordEncoder          passwordEncoder;

    private Integer projectId;
    private String  validToken;

    @BeforeAll
    void setUp() {
        User user = User.builder()
                .name("View").lastName("Test")
                .email("view_" + UUID.randomUUID() + "@test.com")
                .password(passwordEncoder.encode("Pass123!"))
                .role("user").active(true).build();
        userRepository.save(user);

        DesignPlan plan = DesignPlan.builder()
                .name("View Plan " + UUID.randomUUID().toString().substring(0, 4))
                .description("Para tests de viewer")
                .price(BigDecimal.valueOf(29.99)).active(true).build();
        planRepository.save(plan);

        Transaction tx = Transaction.builder()
                .user(user).plan(plan)
                .paymentMethod("transferencia")
                .status("APROBADO").build();
        txRepository.save(tx);

        LandingProject project = LandingProject.builder()
                .transaction(tx)
                .projectName("Landing Test")
                .projectIdea("Idea de prueba")
                .callToAction("Contáctanos")
                .status("Ready")
                .build();
        LandingProject saved = projectRepository.save(project);
        projectId = saved.getProjectId();

        validToken = jwtService.generateLandingToken(projectId);
    }

    @Test @Order(1)
    @DisplayName("GET /landings/{id}?token=valid → 200 con datos del proyecto")
    void shouldReturnProjectDataWithValidToken() throws Exception {
        mvc.perform(get("/landings/" + projectId)
                .param("token", validToken))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.projectId").value(projectId))
           .andExpect(jsonPath("$.status").value("Ready"));
    }

    @Test @Order(2)
    @DisplayName("GET /landings/{id}?token=invalid → 403 con token manipulado")
    void shouldReturn403WithInvalidToken() throws Exception {
        mvc.perform(get("/landings/" + projectId)
                .param("token", "token.invalido.manipulado"))
           .andExpect(status().isForbidden());
    }

    @Test @Order(3)
    @DisplayName("GET /landings/{id}?token=otro_proyecto → 403 token de otro proyecto")
    void shouldReturn403WithTokenFromDifferentProject() throws Exception {
        String otherProjectToken = jwtService.generateLandingToken(99999);
        mvc.perform(get("/landings/" + projectId)
                .param("token", otherProjectToken))
           .andExpect(status().isForbidden());
    }
}