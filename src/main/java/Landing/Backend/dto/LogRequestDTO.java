package Landing.Backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LogRequestDTO {

    @NotNull(message = "El ID del usuario es obligatorio")
    private Integer userId;

    private Integer projectId; // Puede ser null

    @NotBlank(message = "El tipo de evento no puede estar vacío")
    private String eventType;

    private String ipClient;
}