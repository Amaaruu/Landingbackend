package Landing.Backend.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
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

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO requestDTO) {
        User user = new User();
        user.setName(requestDTO.getName());
        user.setLastName(requestDTO.getLastname());
        user.setEmail(requestDTO.getEmail());
        user.setPassword(requestDTO.getPassword());
        user.setRole(requestDTO.getRole());

        User createdUser = userService.saveUser(user);
        return new ResponseEntity<>(convertToResponseDTO(createdUser), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Integer id) {
        // Al no encontrarlo, el servicio lanza ResourceNotFoundException y el GlobalExceptionHandler devuelve el 404
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