package Landing.Backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Landing.Backend.dto.UserRequestDTO;
import Landing.Backend.dto.UserResponseDTO;
import Landing.Backend.model.User;
import Landing.Backend.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // POST: Recibimos el DTO de entrada, lo convertimos a Entidad para guardar
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserRequestDTO requestDTO) {
        User user = new User();
        user.setName(requestDTO.getName());
        user.setLastName(requestDTO.getLastname());
        user.setEmail(requestDTO.getEmail());
        user.setPassword(requestDTO.getPassword());
        user.setRole(requestDTO.getRole());

        User createdUser = userService.saveUser(user);
        return new ResponseEntity<>(convertToResponseDTO(createdUser), HttpStatus.CREATED);
    }

    // GET ALL: Transformamos la lista de Entidades a una lista de DTOs seguros
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.findAllUsers().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Integer id) {
        return userService.findById(id)
                .map(user -> ResponseEntity.ok(convertToResponseDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Integer id, @RequestBody UserRequestDTO requestDTO) {
        try {
            // Reutilizamos la entidad solo como transporte de los datos actualizados
            User userDetails = new User();
            userDetails.setName(requestDTO.getName());
            userDetails.setLastName(requestDTO.getLastname());
            userDetails.setRole(requestDTO.getRole());
            
            User updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(convertToResponseDTO(updatedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        try {
            userService.deleteUser(id); 
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- MÉTODOS DE APOYO (MAPPERS) ---
    // Esto mantiene los endpoints limpios y esparce la lógica de forma profesional.
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