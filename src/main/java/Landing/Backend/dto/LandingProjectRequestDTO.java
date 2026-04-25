package Landing.Backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Data;

@Data
public class LandingProjectRequestDTO {
    
    @NotNull(message = "El ID de la transaccion es obligatorio")
    private Integer transactionId;
    
    @NotBlank(message = "El nombre del proyecto no puede estar vacio")
    private String projectName;
    
    @NotBlank(message = "El sector del negocio es obligatorio")
    private String businessSector;
    
    @NotBlank(message = "El tono de comunicacion no puede estar vacio")
    private String communicationTone;

    @NotNull(message = "Las preferencias de diseño son obligatorias")
    @NotEmpty(message = "Las preferencias de diseño no pueden estar vacias")
    private Map<String, Object> designPreferences; 
}