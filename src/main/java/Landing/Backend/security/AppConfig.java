package Landing.Backend.security;

import Landing.Backend.model.User;
import Landing.Backend.repository.UserRepository;
import Landing.Backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final UserRepository userRepository;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // findByEmail aplica @SQLRestriction("active = true") automáticamente.
            // Si el usuario existe pero está inactivo, se lanza UsernameNotFoundException
            // con mensaje claro en lugar de dejar que Spring Security genere un error genérico.
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> {
                        log.warn("[AppConfig] Intento de autenticación con usuario no encontrado o inactivo: {}", username);
                        return new UsernameNotFoundException(
                            "Usuario no encontrado o inactivo: " + username
                        );
                    });

            log.debug("[AppConfig] Usuario cargado para JWT: {} | rol: {} | activo: {}",
                    user.getEmail(), user.getRole(), user.getActive());

            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRole().toUpperCase())
                    .build();
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}