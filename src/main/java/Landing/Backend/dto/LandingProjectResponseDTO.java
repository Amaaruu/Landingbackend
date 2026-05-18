// src/main/java/Landing/Backend/dto/LandingProjectResponseDTO.java
package Landing.Backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class LandingProjectResponseDTO {

    private Integer projectId;
    private String  projectName;
    private String  projectIdea;
    private String  callToAction;
    private String  businessSector;
    private String  communicationTone;
    private String  landingGoal;
    private String  targetAudience;
    private String  brandPositioning;
    private String  brandStage;
    private String  valueProposition;
    private String  formalityLevel;
    private Map<String, Object> designPreferences;
    private String  status;
    private String  signedUrl;
    private Map<String, Object> aiMetadata;
    private LocalDateTime createdAt;
    private String ownerName;
    private String ownerEmail;
}