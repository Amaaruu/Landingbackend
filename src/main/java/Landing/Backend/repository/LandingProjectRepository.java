package Landing.Backend.repository;

import Landing.Backend.model.LandingProject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LandingProjectRepository extends JpaRepository<LandingProject, Integer> {

    // Proyectos paginados de un usuario concreto
    @Query("SELECT p FROM LandingProject p WHERE p.transaction.user.userId = :userId")
    Page<LandingProject> findByUserId(@Param("userId") Integer userId, Pageable pageable);

    // Verificar si un proyecto pertenece a un usuario
    @Query("SELECT p FROM LandingProject p WHERE p.projectId = :projectId AND p.transaction.user.userId = :userId")
    Optional<LandingProject> findByProjectIdAndUserId(@Param("projectId") Integer projectId,
                                                       @Param("userId") Integer userId);

    // Verificar si un email es dueño de un proyecto (para acceso admin-free)
    @Query("SELECT COUNT(p) > 0 FROM LandingProject p WHERE p.projectId = :projectId AND p.transaction.user.email = :email")
    boolean existsByProjectIdAndUserEmail(@Param("projectId") Integer projectId,
                                          @Param("email") String email);
}