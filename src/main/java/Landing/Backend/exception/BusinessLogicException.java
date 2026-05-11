package Landing.Backend.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class BusinessLogicException extends RuntimeException {
    private final HttpStatus status;

    public BusinessLogicException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}