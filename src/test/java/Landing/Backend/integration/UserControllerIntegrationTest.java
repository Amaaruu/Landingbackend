package Landing.Backend.integration;

import Landing.Backend.AbstractIntegrationTest;
import Landing.Backend.model.User;
import Landing.Backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DisplayName("UserController — pruebas de integración")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc         mvc;
    @Autowired private ObjectMapper    objectMapper;
    @Autowired private UserRepository  userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String adminToken;
    private Integer targetUserId;

    @BeforeAll
    void setUp() throws Exception {
        User admin = User.builder()
                .name("Admin").lastName("UC")
                .email("admin_uc_" + UUID.randomUUID() + "@test.com")
                .password(passwordEncoder.encode("Admin123!"))
                .role("admin").active(true).build();
        userRepository.save(admin);

        User target = User.builder()
                .name("Target").lastName("User")
                .email("target_uc_" + UUID.randomUUID() + "@test.com")
                .password(passwordEncoder.encode("Pass123!"))
                .role("user").active(true).build();
        User savedTarget = userRepository.save(target);
        targetUserId = savedTarget.getUserId();

        MvcResult result = mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"%s","password":"Admin123!"}
                    """.formatted(admin.getEmail())))
           .andExpect(status().isOk()).andReturn();
        adminToken = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test @Order(1)
    @DisplayName("GET /api/v1/users → 200 con lista de usuarios (ADMIN)")
    void shouldGetAllUsers() throws Exception {
        mvc.perform(get("/api/v1/users")
                .header("Authorization", "Bearer " + adminToken))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test @Order(2)
    @DisplayName("GET /api/v1/users/{id} → 200 con datos del usuario (ADMIN)")
    void shouldGetUserById() throws Exception {
        mvc.perform(get("/api/v1/users/" + targetUserId)
                .header("Authorization", "Bearer " + adminToken))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.userId", is(targetUserId)))
           .andExpect(jsonPath("$.name", notNullValue()));
    }

    @Test @Order(3)
    @DisplayName("GET /api/v1/users/{id} → 404 para ID inexistente (ADMIN)")
    void shouldReturn404ForNonExistentUser() throws Exception {
        mvc.perform(get("/api/v1/users/99999")
                .header("Authorization", "Bearer " + adminToken))
           .andExpect(status().isNotFound());
    }

    @Test @Order(4)
    @DisplayName("PUT /api/v1/users/{id} → 200 actualiza nombre y apellido (ADMIN)")
    void shouldUpdateUser() throws Exception {
        mvc.perform(put("/api/v1/users/" + targetUserId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"NuevoNombre","lastname":"NuevoApellido"}
                    """))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.name", is("NuevoNombre")));
    }

    @Test @Order(5)
    @DisplayName("PUT /api/v1/users/{id}/role → 200 cambia rol del usuario (ADMIN)")
    void shouldUpdateUserRole() throws Exception {
        mvc.perform(put("/api/v1/users/" + targetUserId + "/role")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"role":"admin"}
                    """))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.role", is("admin")));
    }

    @Test @Order(6)
    @DisplayName("DELETE /api/v1/users/{id} → 204 elimina usuario (soft delete) (ADMIN)")
    void shouldDeleteUser() throws Exception {
        mvc.perform(delete("/api/v1/users/" + targetUserId)
                .header("Authorization", "Bearer " + adminToken))
           .andExpect(status().isNoContent());
    }

    @Test @Order(7)
    @DisplayName("GET /api/v1/users/{id} → 404 después del soft delete")
    void shouldReturn404AfterSoftDelete() throws Exception {
        mvc.perform(get("/api/v1/users/" + targetUserId)
                .header("Authorization", "Bearer " + adminToken))
           .andExpect(status().isNotFound());
    }
}