package Landing.Backend.service;

import Landing.Backend.dto.TransactionRequestDTO;
import Landing.Backend.exception.BusinessLogicException;
import Landing.Backend.exception.ResourceNotFoundException;
import Landing.Backend.model.DesignPlan;
import Landing.Backend.model.Transaction;
import Landing.Backend.model.User;
import Landing.Backend.repository.DesignPlanRepository;
import Landing.Backend.repository.TransactionRepository;
import Landing.Backend.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService — lógica de creación y consulta de transacciones")
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private DesignPlanRepository designPlanRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User    testUser;
    private DesignPlan testPlan;

    @BeforeEach
    void setUpFixtures() {
        testUser = new User();
        testUser.setUserId(1);
        testUser.setEmail("user@test.com");
        testUser.setActive(true);

        testPlan = new DesignPlan();
        testPlan.setPlanId(10);
        testPlan.setName("Básico");
        testPlan.setPrice(BigDecimal.valueOf(29.99));

        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getName()).thenReturn("user@test.com");
        lenient().when(auth.isAuthenticated()).thenReturn(true);

        SecurityContext context = mock(SecurityContext.class);
        lenient().when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("createTransaction()")
    class CreateTransaction {

        @Test
        @DisplayName("crea una transacción correctamente para usuario y plan válidos")
        void shouldCreateTransactionSuccessfully() {
            // Arrange
            TransactionRequestDTO dto = new TransactionRequestDTO();
            dto.setPlanId(10);

            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
            when(designPlanRepository.findById(10)).thenReturn(Optional.of(testPlan));

            Transaction saved = new Transaction();
            saved.setTransactionId(100);
            saved.setUser(testUser);
            saved.setPlan(testPlan);
            saved.setStatus("PENDIENTE");
            when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

            Transaction result = transactionService.createTransaction(dto);

            assertThat(result.getTransactionId()).isEqualTo(100);
            assertThat(result.getUser().getEmail()).isEqualTo("user@test.com");
            verify(transactionRepository, times(1)).save(any(Transaction.class));
        }

        @Test
        @DisplayName("lanza ResourceNotFoundException si el plan no existe")
        void shouldThrowWhenPlanNotFound() {
            TransactionRequestDTO dto = new TransactionRequestDTO();
            dto.setPlanId(999);

            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
            when(designPlanRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.createTransaction(dto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("lanza BusinessLogicException si el usuario no está autenticado")
        void shouldThrowWhenUserNotAuthenticated() {
            SecurityContextHolder.clearContext(); // sin contexto

            TransactionRequestDTO dto = new TransactionRequestDTO();
            dto.setPlanId(10);

            assertThatThrownBy(() -> transactionService.createTransaction(dto))
                    .isInstanceOf(BusinessLogicException.class);
        }
    }

    @Nested
    @DisplayName("getMyTransactions()")
    class GetMyTransactions {

        @Test
        @DisplayName("retorna lista de transacciones del usuario autenticado")
        void shouldReturnUserTransactions() {
            Transaction t1 = new Transaction();
            t1.setTransactionId(1);
            t1.setUser(testUser);

            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
            when(transactionRepository.findByUser_UserId(1)).thenReturn(List.of(t1));

            List<Transaction> result = transactionService.getTransactionsByAuthenticatedUser();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTransactionId()).isEqualTo(1);
        }
    }
}