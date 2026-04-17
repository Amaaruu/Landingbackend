package Landing.Backend.dto;

import lombok.Data;

@Data
public class LandingProjectRequestDTO {
    private Integer transactionId;
    private String projectName;
    private String businessSector;
    private String communicationTone;
    private String colorPalette; // JSON String
}