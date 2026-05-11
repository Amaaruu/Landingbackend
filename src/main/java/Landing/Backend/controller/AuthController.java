// Archivo: src/main/java/Landing/Backend/controller/AuthController.java
package Landing.Backend.controller;

import Landing.Backend.dto.AuthResponseDTO;
import Landing.Backend.dto.LoginRequestDTO;
import Landing.Backend.dto.UserRequestDTO;
import Landing.Backend.model.User;
import Landing.Backend.repository.UserRepository;
import Landing.Backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para Login y Registro de usuarios")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; 
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody UserRequestDTO request) {
        // 1. Validar que el email no exista previamente
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        // 2. Crear el usuario encriptando la contraseña antes de guardar
        User user = User.builder()
                .name(request.getName())
                .lastName(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) 
                .role(request.getRole())
                .active(true)
                .build();
        
        userRepository.save(user);

        // 3. Generar token automático para iniciar sesión inmediatamente tras registro
        var userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();

        String jwtToken = jwtService.generateToken(userDetails);
        
        return ResponseEntity.ok(AuthResponseDTO.builder()
                .token(jwtToken)
                .message("Registro exitoso")
                .name(user.getName())
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO request) {
        // 1. Validar credenciales (Spring Security lanza error 403 automáticamente si fallan)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        // 2. Recuperar el usuario desde la base de datos
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // 3. Preparar la identidad y generar el JWT
        var userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();

        String jwtToken = jwtService.generateToken(userDetails); 
        
        return ResponseEntity.ok(AuthResponseDTO.builder()
                .token(jwtToken)
                .message("Login exitoso")
                .name(user.getName())
                .build());
    }
}