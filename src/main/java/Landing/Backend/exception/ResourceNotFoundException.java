package Landing.Backend.exception;

// Excepción personalizada para manejar exclusivamente los errores 404 (Not Found)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}