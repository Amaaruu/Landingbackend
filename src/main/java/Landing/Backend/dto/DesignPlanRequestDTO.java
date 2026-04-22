package Landing.Backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class DesignPlanRequestDTO {
    
    @NotBlank(message = "El nombre del plan no puede estar vacio")
    @Size(min = 3, max = 50, message = "El nombre del plan debe tener entre 3 y 50 caracteres")
    private String name;
    
    @NotBlank(message = "La descripcion del plan no puede estar vacia")
    private String description;
    
    @NotBlank(message = "El precio del plan es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio del plan debe ser un valor positivo")
    private BigDecimal price;
}