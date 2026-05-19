package Landing.Backend.exception;

import Landing.Backend.dto.ErrorResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessLogic(BusinessLogicException ex) {
        log.warn("[BusinessLogicException] status={} | mensaje={}", ex.getStatus(), ex.getMessage());
        return buildResponse(ex.getMessage(), ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        log.warn("[ValidationException] campo inválido: {}", msg);
        return buildResponse(msg, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(ResourceNotFoundException ex) {
        log.warn("[ResourceNotFoundException] {}", ex.getMessage());
        // Mensaje genérico: no exponer IDs ni nombres de clases al cliente
        return buildResponse("El recurso solicitado no fue encontrado.", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(Exception ex) {
        String errorId = UUID.randomUUID().toString();
        // Stack trace completo queda en los logs internos de Render, nunca en la respuesta HTTP
        log.error("[Error ID: {}] Excepción no controlada: {}", errorId, ex.getMessage(), ex);
        return buildResponse(
            "Ha ocurrido un error inesperado. Código de referencia: " + errorId,
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private ResponseEntity<ErrorResponseDTO> buildResponse(String message, HttpStatus status) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build();
        return new ResponseEntity<>(error, status);
    }
}