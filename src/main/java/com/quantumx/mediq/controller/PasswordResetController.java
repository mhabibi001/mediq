package com.quantumx.mediq.controller;

import com.quantumx.mediq.dto.PasswordResetRequest;
import com.quantumx.mediq.model.User;
import com.quantumx.mediq.repository.UserRepository;
import com.quantumx.mediq.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest request) {
        try {
            if ((request.getUsername() == null || request.getUsername().trim().isEmpty()) &&
                    (request.getEmail() == null || request.getEmail().trim().isEmpty())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Error: Either username or email must be provided.");
            }

            // Find user by username or email
            Optional<User> userOptional = Optional.empty();
            if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
                userOptional = userRepository.findByUsername(request.getUsername());
            }
            if (userOptional.isEmpty() && request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                userOptional = userRepository.findByEmail(request.getEmail());
            }

            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Error: No user found with the provided username or email.");
            }

            User user = userOptional.get();

            // Generate a temporary password
            String tempPassword = UUID.randomUUID().toString().substring(0, 8);
            user.setPassword(passwordEncoder.encode(tempPassword));
            user.setForcePasswordChange(true);
            userRepository.save(user);

            // Log or send the new password via email
            System.out.println("New password for " + user.getEmail() + ": " + tempPassword);

            emailService.sendEmail(user.getEmail(), "MedIQ - Password Reset Request",
                    "<!DOCTYPE html>\n" +
                            "<html>\n" +
                            "<head>\n" +
                            "    <meta charset=\"UTF-8\">\n" +
                            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                            "    <title>Password Reset</title>\n" +
                            "    <style>\n" +
                            "        body {\n" +
                            "            font-family: Arial, sans-serif;\n" +
                            "            background-color: #f4f4f4;\n" +
                            "            padding: 20px;\n" +
                            "        }\n" +
                            "        .email-container {\n" +
                            "            max-width: 600px;\n" +
                            "            margin: auto;\n" +
                            "            background: #ffffff;\n" +
                            "            padding: 20px;\n" +
                            "            border-radius: 8px;\n" +
                            "            box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n" +
                            "            text-align: center;\n" +
                            "        }\n" +
                            "        .button {\n" +
                            "            display: inline-block;\n" +
                            "            background-color: #007bff;\n" +
                            "            color: #ffffff;\n" +
                            "            padding: 12px 20px;\n" +
                            "            text-decoration: none;\n" +
                            "            border-radius: 5px;\n" +
                            "            font-size: 16px;\n" +
                            "            margin-top: 20px;\n" +
                            "        }\n" +
                            "        .footer {\n" +
                            "            margin-top: 20px;\n" +
                            "            font-size: 12px;\n" +
                            "            color: #666666;\n" +
                            "        }\n" +
                            "    </style>\n" +
                            "</head>\n" +
                            "<body>\n" +
                            "    <div class=\"email-container\">\n" +
                            "        <h2>Password Reset Request</h2>\n" +
                            "        <p>You recently requested to reset your password. Your temporary password is: <b>" + tempPassword + "</b></p>\n" +
                            "        <p>If you did not request this, please ignore this email.</p>\n" +
                            "        <p class=\"footer\">QuantumX Group LLC. All rights reserved. <br></a></p>\n" +
                            "    </div>\n" +
                            "</body>\n" +
                            "</html>");

            return ResponseEntity.ok("A temporary password has been set. Please check your email.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: Unable to process password reset request. Please try again later.");
        }
    }




    private String generateTempPassword() {
        return String.valueOf(new Random().nextInt(999999)); // Generates a 6-digit temp password
    }
}
