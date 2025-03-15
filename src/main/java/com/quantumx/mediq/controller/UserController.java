package com.quantumx.mediq.controller;

import com.quantumx.mediq.model.User;
import com.quantumx.mediq.repository.UserRepository;
import com.quantumx.mediq.service.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final S3Service s3Service;

    @PostMapping("/upload-profile-picture")
    @Transactional
    public ResponseEntity<?> uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found.");
        }

        User user = optionalUser.get();

        // Upload to S3
        String s3Url = s3Service.uploadFile("profile-pictures", file);
        user.setProfilePictureUrl(s3Url);
        userRepository.save(user);

        return ResponseEntity.ok().body("Profile picture uploaded successfully: " + s3Url);
    }
}
