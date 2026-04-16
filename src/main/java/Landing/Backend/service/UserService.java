package Landing.Backend.service;

import org.springframework.stereotype.Service;
import Landing.Backend.model.User;
import Landing.Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll(); //Solo traera a los que tengan active = true
    }

    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    // El controlador puede usar el DELETE tradicional, pero el Servicio hara el Borrado Lógico
    public void deleteUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        //Al llamar a esto, Hibernate ejecutará el UPDATE gracias al @SQLDelete
        userRepository.delete(user);
    }

    //Logica para actualizar (PUT)
    public User updateUser(Integer id, User userDetails) {
        return userRepository.findById(id).map(user -> {
            user.setName(userDetails.getName());
            user.setLastName(userDetails.getLastName());
            user.setRole(userDetails.getRole());
            // No actualizamos email ni password por seguridad en este endpoint
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }
}