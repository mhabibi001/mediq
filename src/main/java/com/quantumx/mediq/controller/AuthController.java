package com.quantumx.mediq.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantumx.mediq.model.EducationLevel;
import com.quantumx.mediq.model.ExamCategory;
import com.quantumx.mediq.model.TimeZone;
import com.quantumx.mediq.model.User;
import com.quantumx.mediq.repository.EducationLevelRepository;
import com.quantumx.mediq.repository.ExamCategoryRepository;
import com.quantumx.mediq.repository.TimeZoneRepository;
import com.quantumx.mediq.repository.UserRepository;
import com.quantumx.mediq.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.util.*;

@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;
    private final ExamCategoryRepository examCategoryRepository;
    private final TimeZoneRepository timeZoneRepository;
    private final EducationLevelRepository educationLevelRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AmazonS3 s3Client;
    @Value("${aws.s3.bucket}")
    private String S3_BUCKET_NAME;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String identifier = loginRequest.get("identifier"); // Can be email or username
        String password = loginRequest.get("password");

        System.out.println("Login attempt with identifier: " + identifier);

        Optional<User> optionalUser = userRepository.findByUsername(identifier);

        if (optionalUser.isEmpty()) {
            optionalUser = userRepository.findByEmail(identifier);
        }

        if (optionalUser.isEmpty()) {
            System.out.println("User not found for identifier: " + identifier);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found"));
        }

        User user = optionalUser.get();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), password)
            );
        } catch (Exception e) {
            System.out.println("Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }

        String token = jwtUtils.generateToken(user);
        System.out.println("Generated JWT Token: " + token);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        System.out.println("User loaded: " + userDetails.getUsername());
        System.out.println("Roles: " + userDetails.getAuthorities());


        return ResponseEntity.ok(Map.of(
                "token", token,
                "role", user.getRole(),
                "forcePasswordChange", user.isForcePasswordChange()
        ));
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(
            @RequestPart("user") String userJson,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {

        System.out.println("Received request at /register");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        User user;
        try {
            user = objectMapper.readValue(userJson, User.class);
        } catch (JsonProcessingException e) {
            System.err.println("JSON Parsing Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid JSON format", "details", e.getMessage()));
        }

        // ✅ Ensure firstName and lastName are provided
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "First name is required."));
        }

        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Last name is required."));
        }

        if (userRepository.findByUsername(user.getUsername()).isPresent() ||
                userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "User already exists."));
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");

        // ✅ Fetch and assign Education Level before saving
        if (user.getEducationLevelId() != null) {
            EducationLevel educationLevel = educationLevelRepository.findById(user.getEducationLevelId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid education level ID"));
            user.setEducationLevelId(user.getEducationLevelId());
        }

        // ✅ Fetch and assign Time Zone before saving
        if (user.getTimeZoneId() != null) {
            TimeZone timeZone = timeZoneRepository.findById(user.getTimeZoneId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid time zone ID"));
            user.setTimeZone(user.getTimeZone());
        }

        // ✅ Save the user BEFORE setting Exam Categories
        user = userRepository.save(user);
        System.out.println("User saved with ID: " + user.getId());

        // ✅ Handle Exam Categories AFTER Saving User
        if (user.getExamCategoryIds() != null && !user.getExamCategoryIds().isEmpty()) {
            Set<ExamCategory> selectedCategories = new HashSet<>(examCategoryRepository.findAllById(user.getExamCategoryIds()));
            user.setExamCategories(selectedCategories);

            user = userRepository.save(user); // Save again to persist relationships
            System.out.println("Exam Categories Linked After Save: " + user.getExamCategories().size());
        } else {
            System.out.println("No Exam Categories provided.");
        }

        // ✅ Handle Profile Picture Upload
        if (profilePicture != null && !profilePicture.isEmpty()) {
            try {
                String uniqueFilename = System.currentTimeMillis() + "_" + profilePicture.getOriginalFilename();
                String s3Key = "profile-pictures/" + uniqueFilename;

                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(profilePicture.getSize());
                s3Client.putObject(S3_BUCKET_NAME, s3Key, profilePicture.getInputStream(), metadata);

                String profilePictureUrl = s3Client.getUrl(S3_BUCKET_NAME, s3Key).toString();
                user.setProfilePictureUrl(profilePictureUrl);
                userRepository.save(user);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Profile picture upload failed."));
            }
        } else {
            System.out.println("No profile picture uploaded.");
        }

        System.out.println("User registered successfully!");
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> request, Authentication authentication) {
        log.info("🔵 Received request to update password");

        if (authentication == null) {
            log.error("🚨 Authentication is NULL!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: Authentication required.");
        }

        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Error: Missing oldPassword or newPassword");
        }

        String username = authentication.getName();
        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            log.error("🚨 User not found: {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: User not found.");
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.error("❌ Incorrect current password for user: {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: Current password is incorrect.");
        }

        // ✅ Update password and force the user to re-authenticate
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setForcePasswordChange(false);
        userRepository.save(user);

        log.info("✅ Password updated successfully for user: {}", username);

        // ✅ Return a response indicating the frontend should request re-login
        return ResponseEntity.ok(Map.of("message", "Password updated. Please log in again."));
    }

}
