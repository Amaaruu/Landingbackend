package Landing.Backend.dto;

import java.util.Map;
import lombok.Data;

@Data
public class AiResponseDTO {
    private String status;
    private Integer projectId;
    private String plan_usado;
    private String ai_engine;
    private Map<String, Object> aiMetadata; 
}