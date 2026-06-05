package Landing.Backend.integration;

import Landing.Backend.AbstractIntegrationTest;
import Landing.Backend.dto.UserRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@AutoConfigureMockMvc
@DisplayName("Auth API — pruebas de integración")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("regression")
public class AuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc        mvc;
    @Autowired private ObjectMapper  objectMapper;

    private static final String BASE_URL  = "/api/v1/auth";
    private static final String TEST_PASS  = "Password123!";
    private String testEmail;

    @BeforeAll
    void generateDynamicEmail() {
        testEmail = "integration_" + UUID.randomUUID().toString() + "@test.com";
    }

    @Test
    @Order(1)
    @DisplayName("POST /register → 200 con token JWT en respuesta")
    void shouldRegisterUserSuccessfully() throws Exception {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Test");
        dto.setLastname("User");
        dto.setEmail(testEmail);
        dto.setPassword(TEST_PASS);

        mvc.perform(post(BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.token", notNullValue()))
           .andExpect(jsonPath("$.name", is("Test")));
    }

    @Test
    @Order(2)
    @DisplayName("POST /register → 409 si email ya existe")
    void shouldRejectDuplicateEmail() throws Exception {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Test");
        dto.setLastname("User");
        dto.setEmail(testEmail);
        dto.setPassword(TEST_PASS);

        mvc.perform(post(BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
           .andExpect(status().isConflict());
    }

    @Test
    @Order(3)
    @DisplayName("POST /login → 200 con token JWT válido")
    void shouldLoginSuccessfully() throws Exception {
        mvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "email": "%s", "password": "%s" }
                    """.formatted(testEmail, TEST_PASS)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.token", notNullValue()))
           .andExpect(jsonPath("$.name",  notNullValue()));
    }

    @Test
    @Order(4)
    @DisplayName("POST /login → 401 con credenciales incorrectas")
    void shouldReturn401ForWrongPassword() throws Exception {
        mvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "email": "%s", "password": "WrongPassword" }
                    """.formatted(testEmail)))
           .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(5)
    @DisplayName("POST /register → 400 con email con formato inválido")
    void shouldRejectInvalidEmailFormat() throws Exception {
        mvc.perform(post(BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "name":"Test","lastname":"U","email":"not-an-email","password":"Pass123!" }
                    """))
           .andExpect(status().isBadRequest());
    }

    @Test @Order(6)
    @DisplayName("POST /register → procesa correctamente con header X-Real-IP")
    void shouldRegisterWithRealIpHeader() throws Exception {
        String uniqueEmail = "realip_" + UUID.randomUUID().toString().replace("-", "") + "@test.com";

        mvc.perform(post(BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Real-IP", "198.51.100.10")
                .content("""
                    {
                      "name":"RealIp",
                      "lastname":"Test",
                      "email":"%s",
                      "password":"Pass123!"
                    }
                    """.formatted(uniqueEmail)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.token").isNotEmpty());
    }
}