package Landing.Backend.service;

import Landing.Backend.dto.LandingProjectRequestDTO;
import Landing.Backend.dto.LandingProjectResponseDTO;
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

    @Nested
    @DisplayName("getUserPlanSafe() normalización de nombre de plan")
    class NormalizePlanName {

        @Test
        @DisplayName("normaliza 'Básico' → 'BASIC'")
        void shouldNormalizeBasico() {
            when(transactionRepository.findById(1)).thenReturn(Optional.of(testTransaction));
            assertThat(landingProjectService.getUserPlanSafe(1)).isEqualTo("BASIC");
        }

        @Test
        @DisplayName("normaliza 'Intermedio' → 'INTERMEDIATE'")
        void shouldNormalizeIntermedio() {
            testPlan.setName("Intermedio");
            when(transactionRepository.findById(1)).thenReturn(Optional.of(testTransaction));
            assertThat(landingProjectService.getUserPlanSafe(1)).isEqualTo("INTERMEDIATE");
        }

        @Test
        @DisplayName("normaliza valor desconocido → 'BASIC' por defecto")
        void shouldFallbackToBasic() {
            testPlan.setName("Desconocido");
            when(transactionRepository.findById(1)).thenReturn(Optional.of(testTransaction));
            assertThat(landingProjectService.getUserPlanSafe(1)).isEqualTo("BASIC");
        }
    }

    @Nested
    @DisplayName("getProjectById() y getProjectByIdForUser()")
    class GetProjectById {

        @Test
        @DisplayName("getProjectById() retorna DTO cuando el proyecto existe")
        void shouldReturnProjectById() {
            LandingProject project = buildProject();
            when(projectRepository.findById(100)).thenReturn(Optional.of(project));

            LandingProjectResponseDTO result = landingProjectService.getProjectById(100);

            assertThat(result.getProjectId()).isEqualTo(100);
            assertThat(result.getStatus()).isEqualTo("Ready");
        }

        @Test
        @DisplayName("getProjectById() lanza ResourceNotFoundException para ID inexistente")
        void shouldThrowWhenProjectNotFound() {
            when(projectRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> landingProjectService.getProjectById(999))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("getProjectByIdForUser() retorna proyecto cuando pertenece al usuario")
        void shouldReturnProjectForAuthenticatedUser() {
            LandingProject project = buildProject();
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
            when(projectRepository.findByProjectIdAndUserId(100, 1))
                    .thenReturn(Optional.of(project));

            LandingProjectResponseDTO result = landingProjectService.getProjectByIdForUser(100);

            assertThat(result.getProjectId()).isEqualTo(100);
        }

        @Test
        @DisplayName("getProjectByIdForUser() lanza FORBIDDEN si existe pero pertenece a otro usuario")
        void shouldThrowForbiddenWhenProjectBelongsToOtherUser() {
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
            when(projectRepository.findByProjectIdAndUserId(100, 1))
                    .thenReturn(Optional.empty());
            when(projectRepository.existsById(100)).thenReturn(true);

            assertThatThrownBy(() -> landingProjectService.getProjectByIdForUser(100))
                    .isInstanceOf(BusinessLogicException.class)
                    .satisfies(ex -> assertThat(((BusinessLogicException) ex).getStatus())
                            .isEqualTo(HttpStatus.FORBIDDEN));
        }

        @Test
        @DisplayName("getProjectByIdForUser() lanza NOT_FOUND si el proyecto no existe")
        void shouldThrowNotFoundWhenProjectDoesNotExist() {
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
            when(projectRepository.findByProjectIdAndUserId(999, 1))
                    .thenReturn(Optional.empty());
            when(projectRepository.existsById(999)).thenReturn(false);

            assertThatThrownBy(() -> landingProjectService.getProjectByIdForUser(999))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateProjectStatus()")
    class UpdateProjectStatus {

        @Test
        @DisplayName("actualiza el estado del proyecto correctamente")
        void shouldUpdateStatus() {
            LandingProject project = buildProject();
            when(projectRepository.findById(100)).thenReturn(Optional.of(project));
            when(projectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LandingProjectResponseDTO result =
                    landingProjectService.updateProjectStatus(100, "Failed");

            assertThat(result.getStatus()).isEqualTo("Failed");
            verify(projectRepository).save(project);
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException para ID inexistente")
        void shouldThrowWhenProjectNotFound() {
            when(projectRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> landingProjectService.updateProjectStatus(999, "Ready"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteProject() y deleteProjectForUser()")
    class DeleteProject {

        @Test
        @DisplayName("deleteProject() llama a repository.delete() con el proyecto correcto")
        void shouldDeleteProject() {
            LandingProject project = buildProject();
            when(projectRepository.findById(100)).thenReturn(Optional.of(project));

            landingProjectService.deleteProject(100);

            verify(projectRepository).delete(project);
        }

        @Test
        @DisplayName("deleteProject() lanza ResourceNotFoundException para ID inexistente")
        void shouldThrowWhenDeletingNonExistentProject() {
            when(projectRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> landingProjectService.deleteProject(999))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("deleteProjectForUser() elimina cuando el proyecto pertenece al usuario")
        void shouldDeleteProjectForAuthenticatedUser() {
            LandingProject project = buildProject();
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
            when(projectRepository.findByProjectIdAndUserId(100, 1))
                    .thenReturn(Optional.of(project));

            landingProjectService.deleteProjectForUser(100);

            verify(projectRepository).delete(project);
        }

        @Test
        @DisplayName("deleteProjectForUser() lanza FORBIDDEN si el proyecto pertenece a otro usuario")
        void shouldThrowForbiddenWhenDeletingOtherUsersProject() {
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
            when(projectRepository.findByProjectIdAndUserId(100, 1))
                    .thenReturn(Optional.empty());
            when(projectRepository.existsById(100)).thenReturn(true);

            assertThatThrownBy(() -> landingProjectService.deleteProjectForUser(100))
                    .isInstanceOf(BusinessLogicException.class)
                    .satisfies(ex -> assertThat(((BusinessLogicException) ex).getStatus())
                            .isEqualTo(HttpStatus.FORBIDDEN));
        }
    }

    @Nested
    @DisplayName("getProjectEntityById()")
    class GetProjectEntityById {

        @Test
        @DisplayName("retorna la entidad cuando el proyecto existe")
        void shouldReturnEntity() {
            LandingProject project = buildProject();
            when(projectRepository.findById(100)).thenReturn(Optional.of(project));

            LandingProject result = landingProjectService.getProjectEntityById(100);

            assertThat(result.getProjectId()).isEqualTo(100);
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException cuando no existe")
        void shouldThrowWhenNotFound() {
            when(projectRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> landingProjectService.getProjectEntityById(999))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

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

    private LandingProject buildProject() {
        return LandingProject.builder()
                .projectId(100)
                .transaction(testTransaction)
                .projectName("Mi Negocio")
                .projectIdea("Vender café online")
                .callToAction("Contáctanos")
                .status("Ready")
                .build();
    }
}