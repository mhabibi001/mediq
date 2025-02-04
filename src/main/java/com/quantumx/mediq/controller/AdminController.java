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
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Question question = objectMapper.readValue(questionJson, Question.class);

            if (question.getCategory() == null || question.getCategory().getId() == null) {
                return ResponseEntity.badRequest().body("Category is required");
            }

            Category category = categoryRepository.findById(question.getCategory().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            question.setCategory(category);

            if (imageFile != null && !imageFile.isEmpty()) {
                // Validate file type
                String contentType = imageFile.getContentType();
                if (!isValidImageType(contentType)) {
                    return ResponseEntity.badRequest().body("Only JPEG, JPG, and PNG file types are allowed");
                }

                // Validate file size
                if (imageFile.getSize() > 10 * 1024 * 1024) { // 10MB limit
                    return ResponseEntity.badRequest().body("File size must not exceed 10MB");
                }

                // Generate unique filename
                String fileName = Instant.now().toEpochMilli() + "_" + imageFile.getOriginalFilename();
                File file = convertMultiPartToFile(imageFile);
                s3Client.putObject(new PutObjectRequest(bucketName, fileName, file));
                question.setImageUrl(s3Client.getUrl(bucketName, fileName).toString());
                question.setImageFilename(fileName);  // Store the filename
                file.delete();
            }

            questionRepository.save(question);
            return ResponseEntity.ok("Question added successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON format");
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