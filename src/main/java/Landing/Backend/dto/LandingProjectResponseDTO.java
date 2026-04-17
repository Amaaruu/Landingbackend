package Landing.Backend.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class LandingProjectResponseDTO {
    private Integer projectId;
    private Integer transactionId; // Solo devolvemos el ID para evitar anidamiento excesivo
    private String projectName;
    private String businessSector;
    private String communicationTone;
    private String colorPalette;
    private String signedUrl;
    private String urlRole;
    private LocalDateTime urlExpiresAt;
    private String aiMetadata;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}