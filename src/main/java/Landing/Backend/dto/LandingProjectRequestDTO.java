package Landing.Backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Data;

@Data
public class LandingProjectRequestDTO {
    
    @NotNull(message = "El ID de la transaccion es obligatorio")
    private Integer transactionId;
    
    // --- CAMPOS OBLIGATORIOS (Todos los planes) ---
    @NotBlank(message = "El nombre del proyecto no puede estar vacio")
    private String projectName;

    @NotBlank(message = "La idea del proyecto es obligatoria")
    private String projectIdea;
    
    @NotBlank(message = "El llamado a la accion (CTA) es obligatorio")
    private String callToAction;
    
    // --- CAMPOS OPCIONALES (Planes Intermedio y Premium) ---
    private String businessSector;
    private String communicationTone;
    private Map<String, Object> designPreferences; 
}