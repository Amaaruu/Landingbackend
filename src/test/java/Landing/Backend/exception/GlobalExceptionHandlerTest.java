package Landing.Backend.exception;

import Landing.Backend.dto.ErrorResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler — mapeo de excepciones a respuestas HTTP")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("BusinessLogicException → HTTP status del campo status de la excepción")
    void shouldMapBusinessLogicExceptionToItsStatus() {
        BusinessLogicException ex = new BusinessLogicException("Acción no permitida", HttpStatus.FORBIDDEN);
        ResponseEntity<ErrorResponseDTO> response = handler.handleBusinessLogic(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Acción no permitida");
        assertThat(response.getBody().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("ResourceNotFoundException → HTTP 404 con mensaje genérico")
    void shouldMapResourceNotFoundTo404WithGenericMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User con ID 99");
        ResponseEntity<ErrorResponseDTO> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        // Verifica que NO se expone el detalle interno al cliente
        assertThat(response.getBody().getMessage())
                .doesNotContain("99")
                .isEqualTo("El recurso solicitado no fue encontrado.");
    }

    @Test
    @DisplayName("Exception genérica → HTTP 500 con código de referencia UUID")
    void shouldMapGenericExceptionTo500WithReferenceCode() {
        Exception ex = new RuntimeException("Error de base de datos");
        ResponseEntity<ErrorResponseDTO> response = handler.handleGlobalException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).contains("Código de referencia:");
        // Verifica que el mensaje interno no se filtra al cliente
        assertThat(response.getBody().getMessage()).doesNotContain("Error de base de datos");
    }
}