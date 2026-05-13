package Landing.Backend.model;

import java.time.LocalDateTime;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "landing_project")
@SQLDelete(sql = "UPDATE landing_project SET active = false WHERE project_id = ?")
@SQLRestriction("active = true")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LandingProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Integer projectId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;

    @Column(name = "project_name", nullable = false)
    private String projectName;

    @Column(name = "project_idea", nullable = false, columnDefinition = "TEXT")
    private String projectIdea;

    @Column(name = "call_to_action", nullable = false)
    private String callToAction;

    @Column(name = "business_sector")
    private String businessSector;

    @Column(name = "communication_tone")
    private String communicationTone;

    @Column(name = "landing_goal")
    private String landingGoal;

    @Column(name = "target_audience")
    private String targetAudience;

    @Column(name = "brand_positioning")
    private String brandPositioning;

    @Column(name = "brand_stage")
    private String brandStage;

    @Column(name = "value_proposition")
    private String valueProposition;

    @Column(name = "formality_level")
    private String formalityLevel;

    @JdbcTypeCode(SqlTypes.JSON) 
    @Column(name = "design_preferences", columnDefinition = "jsonb")
    private Map<String, Object> designPreferences;

    @Column(name = "signed_url")
    private String signedUrl; 

    @Column(name = "url_role")
    private String urlRole;

    @Column(name = "url_expiration_at")
    private LocalDateTime urlExpiresAt;

    @JdbcTypeCode(SqlTypes.JSON) 
    @Column(name = "ai_metadata", columnDefinition = "jsonb")
    private Map<String, Object> aiMetadata;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}