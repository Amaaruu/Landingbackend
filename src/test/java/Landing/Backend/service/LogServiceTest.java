package Landing.Backend.service;

import Landing.Backend.model.Log;
import Landing.Backend.model.LandingProject;
import Landing.Backend.model.User;
import Landing.Backend.repository.LogRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LogService — registro y consulta de eventos")
class LogServiceTest {

    @Mock private LogRepository logRepository;
    @InjectMocks private LogService logService;

    private User        testUser;
    private Log         testLog;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1);
        testUser.setEmail("user@test.com");

        testLog = Log.builder()
                .logId(1)
                .user(testUser)
                .eventType("USER_LOGIN")
                .ipClient("127.0.0.1")
                .build();
    }

    @Test
    @DisplayName("recordLog() persiste y retorna el log correctamente")
    void shouldRecordLog() {
        when(logRepository.save(testLog)).thenReturn(testLog);

        Log result = logService.recordLog(testLog);

        assertThat(result.getLogId()).isEqualTo(1);
        assertThat(result.getEventType()).isEqualTo("USER_LOGIN");
        verify(logRepository).save(testLog);
    }

    @Test
    @DisplayName("recordEvent() crea y persiste un Log sin lanzar excepciones")
    void shouldRecordEventWithoutException() {
        when(logRepository.save(any(Log.class))).thenReturn(testLog);

        assertThatNoException().isThrownBy(() ->
            logService.recordEvent(testUser, null, "USER_REGISTERED", "192.168.1.1")
        );
        verify(logRepository).save(any(Log.class));
    }

    @Test
    @DisplayName("recordEvent() con IP null usa 'unknown' como fallback")
    void shouldUseUnknownWhenIpIsNull() {
        ArgumentCaptor<Log> captor = ArgumentCaptor.forClass(Log.class);
        when(logRepository.save(captor.capture())).thenReturn(testLog);

        logService.recordEvent(testUser, null, "TEST_EVENT", null);

        assertThat(captor.getValue().getIpClient()).isEqualTo("unknown");
    }

    @Test
    @DisplayName("recordEvent() con proyecto asociado lo vincula al log")
    void shouldLinkProjectToLog() {
        LandingProject project = new LandingProject();
        project.setProjectId(10);

        ArgumentCaptor<Log> captor = ArgumentCaptor.forClass(Log.class);
        when(logRepository.save(captor.capture())).thenReturn(testLog);

        logService.recordEvent(testUser, project, "PROJECT_CREATED", "10.0.0.1");

        assertThat(captor.getValue().getEventType()).isEqualTo("PROJECT_CREATED");
    }

    @Test
    @DisplayName("recordEvent() no lanza excepción si el repositorio falla (catch silencioso)")
    void shouldNotPropagateRepositoryException() {
        when(logRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        assertThatNoException().isThrownBy(() ->
            logService.recordEvent(testUser, null, "FAIL_EVENT", "127.0.0.1")
        );
    }

    @Test
    @DisplayName("getAllLogs() retorna página de logs ordenados por fecha desc")
    void shouldReturnPagedLogs() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Log> page = new PageImpl<>(List.of(testLog));
        when(logRepository.findAllByOrderByEventAtDesc(pageable)).thenReturn(page);

        Page<Log> result = logService.getAllLogs(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getAllLogsUnpaged() retorna todos los logs sin paginación")
    void shouldReturnAllLogsUnpaged() {
        when(logRepository.findAllByOrderByEventAtDesc()).thenReturn(List.of(testLog));

        List<Log> result = logService.getAllLogsUnpaged();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getLogById() retorna Optional con el log cuando existe")
    void shouldReturnLogById() {
        when(logRepository.findById(1)).thenReturn(Optional.of(testLog));

        Optional<Log> result = logService.getLogById(1);

        assertThat(result).isPresent()
                .get()
                .extracting(Log::getEventType)
                .isEqualTo("USER_LOGIN");
    }

    @Test
    @DisplayName("getLogById() retorna Optional vacío cuando el log no existe")
    void shouldReturnEmptyOptionalWhenLogNotFound() {
        when(logRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Log> result = logService.getLogById(999);

        assertThat(result).isEmpty();
    }
}