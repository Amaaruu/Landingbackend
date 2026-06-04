package Landing.Backend.integration;

import Landing.Backend.AbstractIntegrationTest;
import Landing.Backend.dto.UserRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@AutoConfigureMockMvc
@DisplayName("Auth API — pruebas de integración")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc        mvc;
    @Autowired private ObjectMapper  objectMapper;

    private static final String BASE_URL  = "/api/v1/auth";
    private static final String TEST_EMAIL = "integration@test.com";
    private static final String TEST_PASS  = "Password123!";

    @Test
    @Order(1)
    @DisplayName("POST /register → 200 con token JWT en respuesta")
    void shouldRegisterUserSuccessfully() throws Exception {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Test");
        dto.setLastname("User");
        dto.setEmail(TEST_EMAIL);
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
    @DisplayName("POST /register → 400 si email ya existe")
    void shouldRejectDuplicateEmail() throws Exception {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Test");
        dto.setLastname("User");
        dto.setEmail(TEST_EMAIL);
        dto.setPassword(TEST_PASS);

        mvc.perform(post(BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
           .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(3)
    @DisplayName("POST /login → 200 con token JWT válido")
    void shouldLoginSuccessfully() throws Exception {
        mvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "email": "%s", "password": "%s" }
                    """.formatted(TEST_EMAIL, TEST_PASS)))
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
                    """.formatted(TEST_EMAIL)))
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
}