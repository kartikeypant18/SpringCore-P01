package JavaFullstack.AutoSplit.service;

import JavaFullstack.AutoSplit.model.User;
import JavaFullstack.AutoSplit.repository.UserRepository;
import JavaFullstack.AutoSplit.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    public User registerUser(User user) {
        // Hash the plain password before saving
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPasswordHash(hashedPassword);
        return userRepository.save(user);
    }

    public boolean checkPassword(User user, String plainPassword) {
        return BCrypt.checkpw(plainPassword, user.getPasswordHash());
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getCurrentUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        String token = authHeader.substring(7);
        String email = jwtUtils.getUsernameFromToken(token); // JwtUtils ka method

        return userRepository.findByEmail(email);
    }
}
