package Landing.Backend.dto;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiResponseDTO {

    private String status;
    private Integer projectId;
    private Map<String, Object> content;
}