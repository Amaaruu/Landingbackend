package Landing.Backend.dto;

import lombok.Data;

@Data
public class LogRequestDTO {
    private Integer userId;
    private Integer projectId; // Puede ser null
    private String eventType;
    private String ipClient;
}