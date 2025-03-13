package com.matchmaking.backend.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.matchmaking.backend.model.User;
import com.matchmaking.backend.model.UserPhoto;
import com.matchmaking.backend.repository.PhotoRepository;
import com.matchmaking.backend.repository.UserRepository;
import com.matchmaking.backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/user/photos")
@RequiredArgsConstructor
public class UserPhotoController {

    private final Cloudinary cloudinary;
    private final UserRepository userRepository;
    private final PhotoRepository photoRepository;
    private final MessageService messageService;

    @PostMapping("/upload")
    public String uploadPhoto(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                messageService.getMessage("user.not.found"))
        );

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

        UserPhoto photo = new UserPhoto();
        photo.setUser(user);
        photo.setUrl(uploadResult.get("url").toString());
        photo.setPublicId(uploadResult.get("public_id").toString());
        photo.setMain(photoRepository.findByUserId(user.getId()).isEmpty());

        photoRepository.save(photo);

        return messageService.getMessage("photo.upload.success");
    }

    @PostMapping("/{photoId}/setMain")
    public String setMainPhoto(
            Authentication authentication, @PathVariable Long photoId
    ) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                messageService.getMessage("user.not.found"))
        );

        List<UserPhoto> photos = photoRepository.findByUserId(user.getId());

        for (UserPhoto photo : photos) {
            photo.setMain(Objects.equals(photo.getId(), photoId)); //tu może być błąd bo jest id a nie public id????
            photoRepository.save(photo);
        }
        return messageService.getMessage("photo.set.main");
    }

    @DeleteMapping("/{photoId}")
    public String deletePhoto(Authentication authentication, @PathVariable Long photoId) throws IOException {
        UserPhoto photo = photoRepository.findById(photoId).orElseThrow();

        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                messageService.getMessage("user.not.found"))
        );

        if (!Objects.equals(photo.getUser().getId(), user.getId())) {
            return messageService.getMessage("permission.required");
        }

        cloudinary.uploader().destroy(photo.getPublicId(), ObjectUtils.emptyMap());
        photoRepository.delete(photo);
        return messageService.getMessage("photo.delete.success");
    }

    @GetMapping
    public List<UserPhoto> getUserPhotos(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                messageService.getMessage("user.not.found"))
        );

        return photoRepository.findByUserId(user.getId());
    }
}