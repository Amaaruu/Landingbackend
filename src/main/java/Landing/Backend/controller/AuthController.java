package Landing.Backend.controller;

import Landing.Backend.dto.AuthResponseDTO;
import Landing.Backend.dto.LoginRequestDTO;
import Landing.Backend.dto.UserRequestDTO;
import Landing.Backend.model.User;
import Landing.Backend.repository.UserRepository;
import Landing.Backend.security.JwtService;
import Landing.Backend.service.EmailService;
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
    private final EmailService emailService; // Inyección del servicio asíncrono

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody UserRequestDTO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        User user = User.builder()
                .name(request.getName())
                .lastName(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) 
                .role(request.getRole())
                .active(true)
                .build();
        
        userRepository.save(user);

        // Disparo del evento asíncrono (No bloquea la respuesta al cliente)
        emailService.sendWelcomeEmail(user.getEmail(), user.getName());

        var userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole()) 
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
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        var userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole())
                .build();

        String jwtToken = jwtService.generateToken(userDetails); 
        
        return ResponseEntity.ok(AuthResponseDTO.builder()
                .token(jwtToken)
                .message("Login exitoso")
                .name(user.getName())
                .build());
    }
}