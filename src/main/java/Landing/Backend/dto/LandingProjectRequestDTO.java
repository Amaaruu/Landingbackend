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
    
    @NotBlank(message = "La descripcion del proyecto no puede estar vacia")
    private String communicationTone;

    @NotBlank(message = "La paleta de colores es obligatorio")
    @NotEmpty(message = "La paleta de colores no puede estar vacia")
    private Map<String, Object> colorPalette; // JSON String
}