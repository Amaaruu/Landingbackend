package Landing.Backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "landing_project")
@SQLDelete(sql = "UPDATE landing_project SET active = false WHERE project_id = ?")
@SQLRestriction("active = true")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Landing_Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Integer projectId;

    // Relacion 1 a 1: una transaccion libera exaactamente un proyecto.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;

    @Column(name = "project_name", nullable = false)
    private String projectName;

    @Column(name = "business_sector", nullable = false)
    private String businessSector;

    @Column(name = "communication_tone", nullable = false)
    private String communicationTone;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "color_palette", columnDefinition = "jsonb")
    private String colorPalette;

    @Column(name = "signed_url")
    private String signedUrl; // URL temporal para descargar el proyecto

    @Column(name = "url_role")
    private String urlRole; // rol asociado a la URL

    @Column(name = "url_expiration_at")
    private LocalDateTime urlExpiresAt; // fecha de expiracion de la URL

    // Metadata de la IA guardada como JSONB
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_metadata", columnDefinition = "jsonb")
    private String aiMetadata;

    @Column(nullable = false)
    private String status; // Procesando, Listo para descargar, expirado, etc.

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    // Metodos del ciclo de vida de JPA
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
