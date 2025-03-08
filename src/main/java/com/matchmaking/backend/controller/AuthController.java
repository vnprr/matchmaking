package com.matchmaking.backend.controller;

import com.matchmaking.backend.repository.UserRepository;
import com.matchmaking.backend.model.User;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public String register(
            @RequestBody Map<String, String> userData
    ) {
        String username = userData.get("username");
        String password = userData.get("password");

        if (userRepository.findByUsername(username).isPresent()) {
            return "użytkownik z taką nazwą już istnieje"
        }

        User user = new User(null, username, passwordEncoder.encode(password), Role.USER);
        userRepository.save(user);
        return "Succesfully registred";
    }

    @PostMapping("/login")
    public String login(
            @RequestBody Map<String, String> creditentials
    ) {
        String username = creditentials.get("username");
        String password = creditentials.get("password");

    Optional<User> userOptional = userRepository.findByUsername(username);
    if(userOptional.isPresent()&&passwordEncoder.matches(password,userOptional.get().

    getPassword()))
            return "Successfully logged";
    }
    return "Wrong login data";
}