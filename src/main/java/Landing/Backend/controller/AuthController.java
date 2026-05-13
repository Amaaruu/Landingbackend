package Landing.Backend.controller;

import Landing.Backend.dto.AuthResponseDTO;
import Landing.Backend.dto.LoginRequestDTO;
import Landing.Backend.dto.UserRequestDTO;
import Landing.Backend.exception.BusinessLogicException;
import Landing.Backend.model.User;
import Landing.Backend.repository.UserRepository;
import Landing.Backend.security.JwtService;
import Landing.Backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para Login y Registro de usuarios")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody UserRequestDTO request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            if (user.getActive()) {
                throw new BusinessLogicException("El correo ya está registrado", HttpStatus.CONFLICT);
            }
            user.setActive(true);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setName(request.getName());
            user.setLastName(request.getLastname());
            user.setRole(request.getRole());
            userRepository.save(user);
        } else {
            user = User.builder()
                    .name(request.getName())
                    .lastName(request.getLastname())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(request.getRole())
                    .active(true)
                    .build();
            userRepository.save(user);
        }

        emailService.sendWelcomeEmail(user.getEmail(), user.getName());

        var userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole())
                .build();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole());

        String jwtToken = jwtService.generateToken(extraClaims, userDetails);

        return ResponseEntity.ok(AuthResponseDTO.builder()
                .token(jwtToken)
                .message("Registro exitoso")
                .name(user.getName())
                .userId(user.getUserId())
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessLogicException("Usuario no encontrado", HttpStatus.NOT_FOUND));

        var userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole())
                .build();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole());

        String jwtToken = jwtService.generateToken(extraClaims, userDetails);

        return ResponseEntity.ok(AuthResponseDTO.builder()
                .token(jwtToken)
                .message("Login exitoso")
                .name(user.getName())
                .userId(user.getUserId())
                .build());
    }
}