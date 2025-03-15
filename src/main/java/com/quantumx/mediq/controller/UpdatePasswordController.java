//package com.quantumx.mediq.controller;
//
//import com.quantumx.mediq.model.User;
//import com.quantumx.mediq.payload.UpdatePasswordRequest;
//import com.quantumx.mediq.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/auth")
//@RequiredArgsConstructor
//public class UpdatePasswordController {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @PostMapping("/update-password")
//    public ResponseEntity<?> updatePassword(
//            @RequestBody UpdatePasswordRequest request,
//            Authentication authentication) {
//
//        // ✅ Ensure authentication exists
//        if (authentication == null || authentication.getName() == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: User is not authenticated.");
//        }
//
//        String username = authentication.getName();
//        System.out.println("Updating password for: " + username);
//
//        // ✅ Find the user in the database
//        Optional<User> optionalUser = userRepository.findByUsername(username);
//        if (optionalUser.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: User not found.");
//        }
//
//        User user = optionalUser.get();
//
//        // ✅ Check if the old password matches
//        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: Incorrect current password.");
//        }
//
//        // ✅ Update and encode new password
//        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
//        userRepository.save(user);
//
//        return ResponseEntity.ok("Success: Password updated successfully.");
//    }
//}
