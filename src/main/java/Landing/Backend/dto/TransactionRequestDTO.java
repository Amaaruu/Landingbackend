package Landing.Backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransactionRequestDTO {
    
    @NotNull(message = "El ID del usuario es obligatoria para la transaccion")
    private Integer userId;
    
    @NotNull(message = "El ID del plan de diseño es obligatorio")
    private Integer planId;
    
    @NotBlank(message = "El metodo d pago es obligatorio")
    private String paymentMethod;
    
    @NotBlank(message = "El estado de la transaccion es obligatorio")
    private String status;
}