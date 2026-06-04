package Landing.Backend.service;

import Landing.Backend.dto.LandingProjectRequestDTO;
import Landing.Backend.exception.BusinessLogicException;
import Landing.Backend.exception.ResourceNotFoundException;
import Landing.Backend.model.*;
import Landing.Backend.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LandingProjectService — creación y gestión de proyectos")
class LandingProjectServiceTest {

    @Mock private LandingProjectRepository projectRepository;
    @Mock private TransactionRepository    transactionRepository;
    @Mock private UserRepository           userRepository;
    @Mock private AiGenerationTask         aiGenerationTask;

    @InjectMocks private LandingProjectService landingProjectService;

    private User        testUser;
    private DesignPlan  testPlan;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1);
        testUser.setEmail("user@test.com");
        testUser.setActive(true);

        testPlan = new DesignPlan();
        testPlan.setPlanId(1);
        testPlan.setName("Básico");
        testPlan.setPrice(BigDecimal.valueOf(29.99));

        testTransaction = new Transaction();
        testTransaction.setTransactionId(1);
        testTransaction.setUser(testUser);
        testTransaction.setPlan(testPlan);
        testTransaction.setStatus("APROBADO");

        mockAuthentication("user@test.com");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthentication(String email) {
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getName()).thenReturn(email);
        lenient().when(auth.isAuthenticated()).thenReturn(true);

        SecurityContext ctx = mock(SecurityContext.class);
        lenient().when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @Nested
    @DisplayName("saveInitialProject()")
    class SaveInitialProject {

        @Test
        @DisplayName("crea proyecto correctamente cuando transacción pertenece al usuario autenticado")
        void shouldCreateProjectSuccessfully() {
            LandingProjectRequestDTO dto = buildValidDTO();

            when(transactionRepository.findById(1)).thenReturn(Optional.of(testTransaction));
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));

            LandingProject saved = LandingProject.builder()
                    .projectId(100)
                    .transaction(testTransaction)
                    .projectName("Mi Negocio")
                    .status("Processing")
                    .build();
            when(projectRepository.save(any())).thenReturn(saved);

            LandingProject result = landingProjectService.saveInitialProject(dto);

            assertThat(result.getProjectId()).isEqualTo(100);
            assertThat(result.getStatus()).isEqualTo("Processing");
            verify(projectRepository).save(any(LandingProject.class));
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException si la transacción no existe")
        void shouldThrowWhenTransactionNotFound() {
            LandingProjectRequestDTO dto = buildValidDTO();
            when(transactionRepository.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> landingProjectService.saveInitialProject(dto))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("lanza BusinessLogicException si la transacción no pertenece al usuario")
        void shouldThrowWhenTransactionBelongsToDifferentUser() {
            User otherUser = new User();
            otherUser.setUserId(99);
            otherUser.setEmail("other@test.com");

            Transaction foreignTx = new Transaction();
            foreignTx.setTransactionId(1);
            foreignTx.setUser(otherUser);
            foreignTx.setPlan(testPlan);

            LandingProjectRequestDTO dto = buildValidDTO();
            when(transactionRepository.findById(1)).thenReturn(Optional.of(foreignTx));
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> landingProjectService.saveInitialProject(dto))
                    .isInstanceOf(BusinessLogicException.class)
                    .satisfies(ex -> assertThat(((BusinessLogicException) ex).getStatus())
                            .isEqualTo(HttpStatus.FORBIDDEN));
        }
    }

    // ── normalizePlanName — prueba del método package-private via getUserPlanSafe ──
    @Nested
    @DisplayName("getUserPlanSafe() normalización de nombre de plan")
    class NormalizePlanName {

        @Test
        @DisplayName("normaliza 'Básico' → 'BASIC'")
        void shouldNormalizeBasico() {
            when(transactionRepository.findById(1)).thenReturn(Optional.of(testTransaction));
            String result = landingProjectService.getUserPlanSafe(1);
            assertThat(result).isEqualTo("BASIC");
        }

        @Test
        @DisplayName("normaliza 'Intermedio' → 'INTERMEDIATE'")
        void shouldNormalizeIntermedio() {
            testPlan.setName("Intermedio");
            when(transactionRepository.findById(1)).thenReturn(Optional.of(testTransaction));
            String result = landingProjectService.getUserPlanSafe(1);
            assertThat(result).isEqualTo("INTERMEDIATE");
        }

        @Test
        @DisplayName("normaliza valor desconocido → 'BASIC' por defecto")
        void shouldFallbackToBasic() {
            testPlan.setName("Desconocido");
            when(transactionRepository.findById(1)).thenReturn(Optional.of(testTransaction));
            String result = landingProjectService.getUserPlanSafe(1);
            assertThat(result).isEqualTo("BASIC");
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private LandingProjectRequestDTO buildValidDTO() {
        LandingProjectRequestDTO dto = new LandingProjectRequestDTO();
        dto.setTransactionId(1);
        dto.setProjectName("Mi Negocio");
        dto.setProjectIdea("Vender café online");
        dto.setCallToAction("Compra ahora");
        dto.setBusinessSector("gastronomia");
        dto.setCommunicationTone("cercano");
        dto.setLandingGoal("ventas");
        return dto;
    }
}