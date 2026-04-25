package Landing.Backend.dto;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LandingProjectResponseDTO {
    private Integer projectId;
    private Integer transactionId; // Solo devolvemos el ID para evitar anidamiento excesivo
    private String projectName;
    private String businessSector;
    private String communicationTone;
    private Map<String, Object> colorPalette;
    private String signedUrl;
    private String urlRole;
    private LocalDateTime urlExpiresAt;
    private Map<String, Object> aiMetadata;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String generatedHtml;
}