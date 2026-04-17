package Landing.Backend.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TransactionResponseDTO {
    private Integer transactionId;
    private Integer userId; 
    private String userName; // Opcional: útil para que el Frontend muestre el nombre directamente
    private Integer planId;
    private String planName;
    private String paymentMethod;
    private String status;
    private LocalDateTime paidAt;
}