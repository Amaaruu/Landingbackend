package Landing.Backend.service;

import Landing.Backend.exception.ResourceNotFoundException;
import Landing.Backend.model.User;
import Landing.Backend.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService — gestión de usuarios")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1);
        testUser.setName("Juan");
        testUser.setLastName("Pérez");
        testUser.setEmail("juan@test.com");
        testUser.setRole("user");
        testUser.setActive(true);
    }

    @Test
    @DisplayName("findAllUsers() delega en findAllActiveUsers() del repositorio")
    void shouldFindAllActiveUsers() {
        when(userRepository.findAllActiveUsers()).thenReturn(List.of(testUser));

        List<User> result = userService.findAllUsers();

        assertThat(result).hasSize(1)
                .first().extracting(User::getEmail).isEqualTo("juan@test.com");
        verify(userRepository).findAllActiveUsers();
    }

    @Test
    @DisplayName("findById() retorna Optional con usuario cuando existe")
    void shouldFindUserById() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findById(1);

        assertThat(result).isPresent()
                .get().extracting(User::getName).isEqualTo("Juan");
    }

    @Test
    @DisplayName("saveUser() persiste y retorna el usuario")
    void shouldSaveUser() {
        when(userRepository.save(testUser)).thenReturn(testUser);

        User result = userService.saveUser(testUser);

        assertThat(result.getEmail()).isEqualTo("juan@test.com");
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("updateUser() actualiza nombre y apellido correctamente")
    void shouldUpdateUser() {
        User updateData = new User();
        updateData.setName("Carlos");
        updateData.setLastName("González");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateUser(1, updateData);

        assertThat(result.getName()).isEqualTo("Carlos");
        assertThat(result.getLastName()).isEqualTo("González");
    }

    @Test
    @DisplayName("updateUser() lanza ResourceNotFoundException para ID inexistente")
    void shouldThrowWhenUpdatingNonExistentUser() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(999, new User()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateUserRole() cambia el rol correctamente")
    void shouldUpdateUserRole() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateUserRole(1, "admin");

        assertThat(result.getRole()).isEqualTo("admin");
    }

    @Test
    @DisplayName("deleteUser() llama a repository.delete() (soft delete via @SQLDelete)")
    void shouldDeleteUser() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        userService.deleteUser(1);

        verify(userRepository).delete(testUser);
    }

    @Test
    @DisplayName("deleteUser() lanza ResourceNotFoundException para ID inexistente")
    void shouldThrowWhenDeletingNonExistentUser() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}