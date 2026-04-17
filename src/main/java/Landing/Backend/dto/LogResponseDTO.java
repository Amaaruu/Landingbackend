package Landing.Backend.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class LogResponseDTO {
    private Integer logId;
    private Integer userId;
    private String userEmail;
    private Integer projectId;
    private String eventType;
    private String ipClient;
    private LocalDateTime eventAt;
}