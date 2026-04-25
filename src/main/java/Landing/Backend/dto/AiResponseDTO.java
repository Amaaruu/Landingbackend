package Landing.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiResponseDTO {
    private String status;
    private Integer projectId;
    private String plan_usado;
    private String ai_engine;
    private String generatedHtml;
    private String message;
}
