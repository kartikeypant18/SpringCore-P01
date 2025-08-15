package JavaFullstack.AutoSplit.controller;

import JavaFullstack.AutoSplit.model.User;
import JavaFullstack.AutoSplit.service.UserService;
import JavaFullstack.AutoSplit.security.JwtUtils; // make sure this import matches your package
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        User savedUser = userService.registerUser(user); // hashes inside service
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        Optional<User> optionalUser = userService.findByEmail(loginRequest.getEmail());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (userService.checkPassword(user, loginRequest.getPassword())) {
                String token = jwtUtils.generateToken(user.getEmail());

                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("email", user.getEmail());
                response.put("message", "Login successful");

                return ResponseEntity.ok(response);
            }
        }

        return ResponseEntity.status(401).body("Invalid credentials");
    }
}
