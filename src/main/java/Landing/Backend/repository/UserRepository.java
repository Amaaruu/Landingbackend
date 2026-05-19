package Landing.Backend.repository;

import Landing.Backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    // Query nativa para admin: obtiene solo usuarios activos explícitamente
    // Necesario porque @SQLRestriction ya filtra, pero si hay problema de sesión
    // esta query clarifica la intención
    @Query("SELECT u FROM User u WHERE u.active = true ORDER BY u.registeredAt DESC")
    List<User> findAllActiveUsers();

    // Buscar por ID ignorando el filtro de active (para joins internos y auditoría)
    @Query(value = "SELECT * FROM users WHERE user_id = :id", nativeQuery = true)
    Optional<User> findByIdNative(Integer id);
}