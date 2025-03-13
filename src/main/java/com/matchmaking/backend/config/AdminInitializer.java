package com.matchmaking.backend.config;

import com.matchmaking.backend.model.Role;
import com.matchmaking.backend.model.User;
import com.matchmaking.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Map;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
                .getResourceAsStream("createAdmin.yml")
        ) {
            if (inputStream == null) {
                System.out.println("Admin file not found! Nothing created!");
                return;
            }
            Yaml yaml = new Yaml();
            Map<String, Object> adminProperties = yaml.load(
                    inputStream
            );

            boolean createAdmin = Boolean.parseBoolean(
                    adminProperties.get("createAdmin").toString()
            );

            if (!createAdmin) {
                System.out.println("Admin creation disabled in createAdmin.yml! Nothing created!");
                return;
            }

            String adminLogin = adminProperties.get("adminLogin").toString();
            String adminPassword = adminProperties.get("adminPassword").toString();

            if (userRepository.findByUsername(adminLogin).isEmpty()) {
                User admin = new User();

                admin.setUsername(adminLogin);
                admin.setPassword(
                        passwordEncoder.encode(adminPassword)
                );
                admin.setRole(Role.ADMIN);

                userRepository.save(admin);

                System.out.println("\nAdmin user created successfully! 🧙\n");
            }
        } catch (FileNotFoundException e) {
            System.out.println("createAdmin.yml file not found! Nothing created!");
        } catch (Exception e) {
            System.out.println("Error while creating admin user: " + e.getMessage());
        }

    }

}
