package Landing.Backend.dto;

import java.util.Map;

import lombok.Data;

@Data
public class LandingProjectRequestDTO {
    private Integer transactionId;
    private String projectName;
    private String businessSector;
    private String communicationTone;
    private Map<String, Object> colorPalette; // JSON String
}