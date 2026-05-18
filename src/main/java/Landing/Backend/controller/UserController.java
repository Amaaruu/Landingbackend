// src/main/java/Landing/Backend/controller/UserController.java
package Landing.Backend.controller;

import Landing.Backend.dto.RoleUpdateDTO;
import Landing.Backend.dto.UserUpdateDTO;
import Landing.Backend.dto.UserResponseDTO;
import Landing.Backend.exception.ResourceNotFoundException;
import Landing.Backend.model.User;
import Landing.Backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los usuarios (solo admin)")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(
            userService.findAllUsers().stream()
                .map(this::toDTO)
                .collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener usuario por ID (solo admin)")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Integer id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        return ResponseEntity.ok(toDTO(user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar nombre y apellido (solo admin)")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UserUpdateDTO dto) {
        User details = new User();
        details.setName(dto.getName());
        details.setLastName(dto.getLastname());
        return ResponseEntity.ok(toDTO(userService.updateUser(id, details)));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar rol de usuario (solo admin)")
    public ResponseEntity<UserResponseDTO> updateUserRole(
            @PathVariable Integer id,
            @Valid @RequestBody RoleUpdateDTO dto) {
        return ResponseEntity.ok(toDTO(userService.updateUserRole(id, dto.getRole())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar usuario (solo admin)")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private UserResponseDTO toDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setUserId(user.getUserId());
        dto.setUuid(user.getUuid());
        dto.setName(user.getName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setRegisteredAt(user.getRegisteredAt());
        return dto;
    }
}