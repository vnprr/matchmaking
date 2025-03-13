package com.matchmaking.backend.controller;

import com.matchmaking.backend.model.PasswordChangeRequestDTO;
import com.matchmaking.backend.model.User;
import com.matchmaking.backend.model.UserDTO;
import com.matchmaking.backend.repository.UserRepository;
import com.matchmaking.backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageService messageService;

    @GetMapping("/me")
    public UserDTO getCurrentUser(
            Authentication authentication
    ) {
        User user = userRepository.findByUsername(
                authentication.getName()
        ).orElseThrow(() -> new RuntimeException(
                messageService.getMessage("user.not.found"))
        );

        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    // Update profile
    @PutMapping("/me")
    public String updateUser(
            Authentication authentication,
            @RequestBody UserDTO userDTO) {

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException(
                        messageService.getMessage("user.not.found")
                ));

        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());

        userRepository.save(user);

        return messageService.getMessage("update.success");
    }

    @PutMapping("/me/password")
    public String updatePassword(
            Authentication authentication,
            @RequestBody PasswordChangeRequestDTO request
    ) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(()-> new RuntimeException(
                        messageService.getMessage("user.not.found")
                ));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return messageService.getMessage("old.password.incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return messageService.getMessage("password.update.success");
    }

    @DeleteMapping("/me")
    public String deleteUser(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new RuntimeException(
                    messageService.getMessage("user.not.found")
            ));

        userRepository.delete(user);

        return messageService.getMessage("delete.success");
    }

}
