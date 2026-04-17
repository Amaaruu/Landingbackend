package Landing.Backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer LogId;

    // Relacion con el Usuario (Obligatorio, siempre sabemos quién hace la acción)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Relacion con el Proyecto (Opcional, no todos los eventos tienen un proyecto asociado)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = true)
    private LandingProject project;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "ip_client")
    private String ipClient;

    @Column(name = "event_at", nullable = false, updatable = false)
    private LocalDateTime eventAt;

    // Se ejecuta automaticamente al guardar el log
    @PrePersist
    protected void onCreate() {
        this.eventAt = LocalDateTime.now();
    }
    
}
