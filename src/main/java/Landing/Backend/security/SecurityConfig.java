package Landing.Backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults()) // Activa tu archivo CorsConfig para que React pueda conectarse
            .csrf(csrf -> csrf.disable()) // Desactivamos CSRF ya que usaremos tokens (JWT)
            .authorizeHttpRequests(auth -> auth
                // RUTAS PÚBLICAS (No requieren token)
                .requestMatchers("/api/v1/auth/**").permitAll() // Login y Registro
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Swagger
                // RUTAS PRIVADAS (Todo lo demás requiere token)
                .anyRequest().authenticated() 
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // Filtro que lee el token antes de dar acceso

        return http.build();
    }
}