package Landing.Backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(unique = true, nullable = false, updatable = false)
    private String uuid;

    private String name;

    @Column(name = "last_name")
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // admin, user

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    //Este metodo se ejecutara automaticamente justo antes de guardar en la base de datos por primera vez.
    @PrePersist
    protected void onCreate() {
        this.registeredAt = LocalDateTime.now();
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString(); // genera el UUID automaticamente.
        }
    }
}
