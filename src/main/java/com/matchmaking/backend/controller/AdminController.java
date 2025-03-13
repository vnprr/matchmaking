package com.matchmaking.backend.controller;

import com.matchmaking.backend.model.Role;
import com.matchmaking.backend.model.User;
import com.matchmaking.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MessageSource messageSource;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Locale DEFAULT_LOCALE = Locale.getDefault();

    @GetMapping("/dashboard")
    public String adminDashboard() {
        return getMessage("admin.login.success");
    }

    private String getMessage(String code) {
        return messageSource.getMessage(code, null, DEFAULT_LOCALE);
    }

    private String createAdmin(
            @RequestBody Map<String, String> adminData
    ) {

        String username = adminData.get("username");
        String password = adminData.get("password");

        if(userRepository.findByUsername(
                username
        ).isPresent()
        ) {
            return getMessage("username.exists");
        }

        User admin = new User();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        return getMessage("admin.login.success");
    }
}
