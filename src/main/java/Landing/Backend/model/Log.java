package Landing.Backend.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = true)
    private LandingProject project;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "ip_client")
    private String ipClient;

    @Column(name = "event_at", nullable = false, updatable = false)
    private LocalDateTime eventAt;

    @PrePersist
    protected void onCreate() {
        this.eventAt = LocalDateTime.now();
    }
}