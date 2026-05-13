package Landing.Backend.dto;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LandingProjectResponseDTO {
    private Integer projectId;
    private String projectName;
    private String businessSector;
    private String communicationTone;
    private Map<String, Object> designPreferences;
    private String signedUrl;
    private Map<String, Object> aiMetadata; 
    private String status;
    private LocalDateTime createdAt;
    private String projectIdea;
    private String callToAction;
    private String landingGoal;
    private String targetAudience;
    private String brandPositioning;
    private String brandStage;
    private String valueProposition;
    private String formalityLevel;
}