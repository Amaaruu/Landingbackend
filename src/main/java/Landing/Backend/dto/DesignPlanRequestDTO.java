package Landing.Backend.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class DesignPlanRequestDTO {
    private String name;
    private String description;
    private BigDecimal price;
}