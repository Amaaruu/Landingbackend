package Landing.Backend.dto;

import lombok.Data;

@Data
public class TransactionRequestDTO {
    private Integer userId;
    private Integer planId;
    private String paymentMethod;
    private String status;
}