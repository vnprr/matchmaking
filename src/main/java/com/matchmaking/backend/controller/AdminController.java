package com.matchmaking.backend.controller;

import com.matchmaking.backend.model.Recommendation;
import com.matchmaking.backend.model.Role;
import com.matchmaking.backend.model.User;
import com.matchmaking.backend.service.MessageService;
import com.matchmaking.backend.repository.RecommendationRepository;
import com.matchmaking.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MessageService messageService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RecommendationRepository recommendationRepository;

    @GetMapping("/dashboard")
    public String adminDashboard() {
        return messageService.getMessage("admin.login.success");
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
            return messageService.getMessage("username.exists");
        }

        User admin = new User();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        return messageService.getMessage("admin.login.success");
    }

    private String recommendUser(
            @RequestParam Long userId,
            @RequestParam String recommendedUserId,
            @RequestParam(required = false) String note
    ) {
        User user = userRepository.findById(userId).orElseThrow(
                ()->new RuntimeException(messageService.getMessage("user.not.found"))
        );
        User recommendedUser = userRepository.findById(userId).orElseThrow();

        Recommendation recommendation = new Recommendation(null, user, recommendedUser, note);
        recommendationRepository.save(recommendation);

        return messageService.getMessage("admin.recommendation.success");

    }
}
