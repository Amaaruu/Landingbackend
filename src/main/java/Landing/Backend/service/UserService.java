// src/main/java/Landing/Backend/service/UserService.java
package Landing.Backend.service;

import Landing.Backend.exception.ResourceNotFoundException;
import Landing.Backend.model.User;
import Landing.Backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    public void deleteUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        userRepository.delete(user);
    }

    /** Actualiza nombre y apellido únicamente. No toca el rol. */
    public User updateUser(Integer id, User userDetails) {
        return userRepository.findById(id).map(user -> {
            if (userDetails.getName() != null)     user.setName(userDetails.getName());
            if (userDetails.getLastName() != null) user.setLastName(userDetails.getLastName());
            // rol NO se toca aquí: usar updateUserRole()
            return userRepository.save(user);
        }).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }

    /** Actualiza el rol únicamente. Endpoint exclusivo para admins. */
    public User updateUserRole(Integer id, String newRole) {
        return userRepository.findById(id).map(user -> {
            user.setRole(newRole);
            return userRepository.save(user);
        }).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }
}