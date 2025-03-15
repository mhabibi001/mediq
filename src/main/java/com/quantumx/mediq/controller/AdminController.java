package com.quantumx.mediq.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantumx.mediq.model.Question;
import com.quantumx.mediq.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private QuestionRepository questionRepository;

    private final AmazonS3 s3Client;

    @Autowired
    public AdminController(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    @Value("${aws.s3.bucket}")
    private String bucketName;

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

            // 🛠️ LOGGING ADDED: Debugging the request
            System.out.println("Received question: " + question);
            System.out.println("Received image file: " + (imageFile != null ? imageFile.getOriginalFilename() : "No file"));
            System.out.println("Received justification file: " + (justificationImageFile != null ? justificationImageFile.getOriginalFilename() : "No file"));

            // ✅ Upload Question Image
            String questionImageUrl = null;
            if (imageFile != null && !imageFile.isEmpty()) {
                String uniqueFilename = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                String s3Key = "question-image/" + uniqueFilename;
                s3Client.putObject(new PutObjectRequest(bucketName, s3Key, imageFile.getInputStream(), null));
                questionImageUrl = s3Client.getUrl(bucketName, s3Key).toString();

                question.setImageUrl(questionImageUrl);
                question.setImageFilename(uniqueFilename);
            }

            // ✅ Upload Justification Image
            String justificationImageUrl = null;
            if (justificationImageFile != null && !justificationImageFile.isEmpty()) {
                String uniqueFilename = System.currentTimeMillis() + "_" + justificationImageFile.getOriginalFilename();
                String s3Key = "justification/" + uniqueFilename;
                s3Client.putObject(new PutObjectRequest(bucketName, s3Key, justificationImageFile.getInputStream(), null));
                justificationImageUrl = s3Client.getUrl(bucketName, s3Key).toString();

                question.setJustificationImageUrl(justificationImageUrl);
                question.setJustificationImageName(uniqueFilename);
            }

            questionRepository.save(question);
            return ResponseEntity.ok("Question added successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing request");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(IllegalArgumentException ex) {
        return ex.getMessage();
    }
}
