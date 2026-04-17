package Landing.Backend.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class DesignPlanResponseDTO {
    private Integer planId;
    private String name;
    private String description;
    private BigDecimal price;
}