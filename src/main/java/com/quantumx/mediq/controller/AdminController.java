package com.quantumx.mediq.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantumx.mediq.model.Category;
import com.quantumx.mediq.model.Question;
import com.quantumx.mediq.repository.CategoryRepository;
import com.quantumx.mediq.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private final AmazonS3 s3Client;

    @Autowired
    public AdminController(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @GetMapping("/categories")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll(); // Fetch all categories from the database
    }

    @PostMapping(value = "/add-question", consumes = "multipart/form-data")
    public ResponseEntity<?> addQuestion(
            @RequestPart("question") String questionJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @RequestPart(value = "justificationImage", required = false) MultipartFile justificationImageFile) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Question question = objectMapper.readValue(questionJson, Question.class);

            if (question.getCategory() == null || question.getCategory().getId() == null) {
                return ResponseEntity.badRequest().body("Category is required");
            }

            // ✅ Upload Question Image
            String questionImageUrl = null;
            if (imageFile != null && !imageFile.isEmpty()) {
                String originalFilename = imageFile.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                if (!fileExtension.matches("\\.(jpeg|jpg|png)$")) {
                    return ResponseEntity.badRequest().body("Invalid file type. Only JPEG, JPG, and PNG are allowed.");
                }
                if (imageFile.getSize() > 10 * 1024 * 1024) {
                    return ResponseEntity.badRequest().body("File size exceeds 10MB limit.");
                }
                String uniqueFilename = System.currentTimeMillis() + "_" + originalFilename;
                String s3Key = "question-image/" + uniqueFilename;
                s3Client.putObject(new PutObjectRequest(bucketName, s3Key, imageFile.getInputStream(), null));
                questionImageUrl = s3Client.getUrl(bucketName, s3Key).toString();

                question.setImageUrl(questionImageUrl);
                question.setImageFilename(uniqueFilename); // ✅ FIX: Ensure filename is saved
            }

            // ✅ Upload Justification Image
            String justificationImageUrl = null;
            if (justificationImageFile != null && !justificationImageFile.isEmpty()) {
                String originalFilename = justificationImageFile.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                if (!fileExtension.matches("\\.(jpeg|jpg|png)$")) {
                    return ResponseEntity.badRequest().body("Invalid file type. Only JPEG, JPG, and PNG are allowed.");
                }
                if (justificationImageFile.getSize() > 10 * 1024 * 1024) {
                    return ResponseEntity.badRequest().body("File size exceeds 10MB limit.");
                }
                String uniqueFilename = System.currentTimeMillis() + "_" + originalFilename;
                String s3Key = "justification/" + uniqueFilename;
                s3Client.putObject(new PutObjectRequest(bucketName, s3Key, justificationImageFile.getInputStream(), null));
                justificationImageUrl = s3Client.getUrl(bucketName, s3Key).toString();

                question.setJustificationImageUrl(justificationImageUrl);
                question.setJustificationImageName(uniqueFilename); // ✅ FIX: Ensure filename is saved
            }

            questionRepository.save(question);
            return ResponseEntity.ok("Question added successfully!");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing request");
        }
    }


    private boolean isValidImageType(String contentType) {
        return contentType != null && (contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/jpg"));
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(IllegalArgumentException ex) {
        return ex.getMessage();
    }
}