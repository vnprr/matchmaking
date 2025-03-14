package com.matchmaking.backend.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.matchmaking.backend.model.User;
import com.matchmaking.backend.model.UserPhoto;
import com.matchmaking.backend.model.UserPhotoDTO;
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

        boolean found = false;
        for (UserPhoto photo : photos) {
            if (photo.getId().equals(photoId)) {
                photo.setMain(true);
                found = true;
            } else {
                photo.setMain(false);
            }
            photoRepository.save(photo);
        }

        return found ? messageService.getMessage("photo.set.main") : messageService.getMessage("photo.not.found");
    }

    @DeleteMapping("/{photoId}")
    public String deletePhoto(
            Authentication authentication,
            @PathVariable Long photoId
    ) throws IOException {

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("user.not.found")));

        Optional<UserPhoto> optionalPhoto = photoRepository.findById(photoId);

        if (optionalPhoto.isEmpty() || !Objects.equals(optionalPhoto.get().getUser().getId(), user.getId())) {
            return messageService.getMessage("permission.required");
        }

        UserPhoto photo = optionalPhoto.get();
        boolean wasMain = photo.getMain();

        cloudinary.uploader().destroy(photo.getPublicId(), ObjectUtils.emptyMap());
        photoRepository.delete(photo);

        if (wasMain) {
            return messageService.getMessage("choose.new.profile.photo");
        }
        return messageService.getMessage("photo.delete.success");
    }

    @GetMapping
    public List<UserPhotoDTO> getUserPhotos(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException(
                messageService.getMessage("user.not.found"))
        );

        return photoRepository.findByUserId(user.getId())
                .stream()
                .map(photo -> new UserPhotoDTO(photo.getId(), photo.getUrl(), photo.getMain()))
                .toList();
    }
}