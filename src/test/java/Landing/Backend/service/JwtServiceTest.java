package Landing.Backend.service;

import Landing.Backend.security.JwtService;
import Landing.Backend.exception.BusinessLogicException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtService — lógica de generación y validación de tokens")
class JwtServiceTest {

    private JwtService jwtService;

    private static final String TEST_SECRET =
        "test-secret-key-must-be-at-least-256-bits-for-hs256";
    private static final long EXPIRATION_MS  = 3_600_000L;
    private static final long LANDING_EXP_MS = 86_400_000L;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey",            TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration",        EXPIRATION_MS);
        ReflectionTestUtils.setField(jwtService, "landingTokenExpiration", LANDING_EXP_MS);
    }

    private UserDetails mockUser(String email) {
        return User.builder()
                .username(email)
                .password("password")
                .roles("USER")
                .build();
    }

    @Nested
    @DisplayName("generateToken()")
    class GenerateToken {

        @Test
        @DisplayName("genera un token no nulo para un usuario válido")
        void shouldGenerateNonNullToken() {
            UserDetails user = mockUser("user@test.com");
            String token = jwtService.generateToken(user);
            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("el token contiene el email correcto como subject")
        void shouldContainCorrectSubject() {
            String email = "user@test.com";
            UserDetails user = mockUser(email);
            String token = jwtService.generateToken(user);
            assertThat(jwtService.extractUsername(token)).isEqualTo(email);
        }
    }

    @Nested
    @DisplayName("isTokenValid()")
    class IsTokenValid {

        @Test
        @DisplayName("devuelve true para token recién generado con mismo usuario")
        void shouldReturnTrueForValidToken() {
            UserDetails user = mockUser("user@test.com");
            String token = jwtService.generateToken(user);
            assertThat(jwtService.isTokenValid(token, user)).isTrue();
        }

        @Test
        @DisplayName("devuelve false cuando el token pertenece a otro usuario")
        void shouldReturnFalseForDifferentUser() {
            UserDetails userA = mockUser("a@test.com");
            UserDetails userB = mockUser("b@test.com");
            String tokenA = jwtService.generateToken(userA);
            assertThat(jwtService.isTokenValid(tokenA, userB)).isFalse();
        }
    }

    @Nested
    @DisplayName("generateLandingToken() / validateLandingToken()")
    class LandingToken {

        @Test
        @DisplayName("genera y valida correctamente un landing token")
        void shouldGenerateAndValidateLandingToken() {
            Integer projectId = 42;
            String token = jwtService.generateLandingToken(projectId);
            assertThat(token).isNotNull();
            assertThat(jwtService.validateLandingToken(token)).isEqualTo(projectId);
        }

        @Test
        @DisplayName("lanza BusinessLogicException para token manipulado")
        void shouldThrowForTamperedToken() {
            String token = jwtService.generateLandingToken(1) + "tampered";
            assertThatThrownBy(() -> jwtService.validateLandingToken(token))
                    .isInstanceOf(BusinessLogicException.class);
        }

        @Test
        @DisplayName("lanza BusinessLogicException para token de usuario normal (tipo incorrecto)")
        void shouldThrowForWrongTokenType() {
            UserDetails user = mockUser("user@test.com");
            String userToken = jwtService.generateToken(user);
            assertThatThrownBy(() -> jwtService.validateLandingToken(userToken))
                    .isInstanceOf(BusinessLogicException.class);
        }
    }
}