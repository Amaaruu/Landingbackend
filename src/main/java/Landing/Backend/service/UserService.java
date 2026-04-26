package Landing.Backend.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import Landing.Backend.model.User;
import Landing.Backend.repository.UserRepository;
import Landing.Backend.exception.ResourceNotFoundException; 
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

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

    public User updateUser(Integer id, User userDetails) {
        return userRepository.findById(id).map(user -> {
            user.setName(userDetails.getName());
            user.setLastName(userDetails.getLastName());
            user.setRole(userDetails.getRole());
            return userRepository.save(user);
        }).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }
}