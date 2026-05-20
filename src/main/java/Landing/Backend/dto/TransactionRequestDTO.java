package Landing.Backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransactionRequestDTO {
    
    @NotNull(message = "El ID del plan de diseño es obligatorio")
    private Integer planId;
    
    @NotBlank(message = "El metodo de pago es obligatorio")
    private String paymentMethod;
    
}