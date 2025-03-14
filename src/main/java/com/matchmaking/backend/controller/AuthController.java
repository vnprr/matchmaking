package com.matchmaking.backend.controller;

import com.matchmaking.backend.config.JwtUtil;
import com.matchmaking.backend.repository.UserRepository;
import com.matchmaking.backend.model.User;
import com.matchmaking.backend.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private static final Locale DEFAULT_LOCALE = Locale.getDefault();

    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> userData) {
        if (!isValidUserData(userData)) {
            return getMessage("user.data.invalid");
        }
        String username = userData.get(USERNAME_KEY);
        String password = userData.get(PASSWORD_KEY);
        if (userRepository.findByUsername(username).isPresent()) {
            return getMessage("registration.username.exists");
        }
        createNewUser(username, password);
        return getMessage("registration.success");
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (passwordEncoder.matches(password, userDetails.getPassword())) {
            String token = jwtUtil.generateToken(userDetails);
            return Map.of("token", token);
        }
        return Map.of("error", "Invalid credentials");
    }

    private boolean areValidUserCredentials(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .isPresent();
    }

    private void createNewUser(String username, String password) {
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole(Role.USER);
        userRepository.save(newUser);
    }

    private boolean isValidUserData(Map<String, String> userData) {
        String username = userData.get(USERNAME_KEY);
        String password = userData.get(PASSWORD_KEY);
        return username != null && !username.isEmpty()
                && password != null && !password.isEmpty();
    }

    private String getMessage(String code) {
        return messageSource.getMessage(code, null, DEFAULT_LOCALE);
    }
}

