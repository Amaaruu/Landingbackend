package Landing.Backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import Landing.Backend.dto.UserRequestDTO;
import Landing.Backend.dto.UserResponseDTO;
import Landing.Backend.model.User;
import Landing.Backend.service.UserService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios")
public class UserController {

    private final UserService userService;

    // Se eliminó el @PostMapping. 
    // Todo registro debe pasar exclusivamente por AuthController para ser encriptado.

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Integer id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new Landing.Backend.exception.ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        return ResponseEntity.ok(convertToResponseDTO(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar perfil")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Integer id, @Valid @RequestBody UserRequestDTO requestDTO) {
        User userDetails = new User();
        userDetails.setName(requestDTO.getName());
        userDetails.setLastName(requestDTO.getLastname());
        userDetails.setRole(requestDTO.getRole());
        
        User updatedUser = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(convertToResponseDTO(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id); 
        return ResponseEntity.noContent().build();
    }

    private UserResponseDTO convertToResponseDTO(User user) {
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