package com.matchmaking.backend.config;

import com.matchmaking.backend.model.Role;
import com.matchmaking.backend.model.User;
import com.matchmaking.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private String name = "admin";
    private String password = "Admin123!";

    public AdminInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        try (InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream("createAdmin.txt")
        ) {
            assert inputStream != null;
            BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(
                    inputStream, StandardCharsets.UTF_8)
            );

            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername(this.name);
                admin.setPassword(this.password);
                admin.setRole(Role.ADMIN);
                UserRepository.save(admin);
                System.out.println("Created admin user");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Admin file not found, nothing created");
        }

    }

}
