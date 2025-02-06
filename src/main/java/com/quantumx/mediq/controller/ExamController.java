package com.quantumx.mediq.controller;

import com.quantumx.mediq.dto.QuestionDTO;
import com.quantumx.mediq.model.Question;
import com.quantumx.mediq.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/exam")
@CrossOrigin(origins = "http://localhost:3000") // Allow frontend access
public class ExamController {

    @Autowired
    private QuestionRepository questionRepository;

    // Endpoint to get the count of questions in a category
    @GetMapping("/questions/count")
    public ResponseEntity<Map<String, Integer>> getQuestionCount(@RequestParam Long categoryId) {
        try {
            int count = questionRepository.countByCategoryId(categoryId); // Ensure this method exists in your repo
            Map<String, Integer> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint to fetch questions by category
    @GetMapping("/questions")
    public List<QuestionDTO> getQuestions(@RequestParam Long categoryId, @RequestParam int count) {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID cannot be null.");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be greater than zero.");
        }

        // Fetch questions from the database
        List<Question> questions = questionRepository.findByCategoryId(categoryId);

        // Check if there are enough questions
        if (questions.isEmpty()) {
            throw new IllegalArgumentException("No questions available for the specified category.");
        }

        // Shuffle the questions
        Collections.shuffle(questions);

        // **Map Question entity to QuestionDTO**
        return questions.stream()
                .limit(count)
                .map(q -> new QuestionDTO(
                        q.getId(),
                        q.getQuestion(),
                        q.getJustification(),
                        List.of(q.getRightAnswer(), q.getWrongAnswer1(), q.getWrongAnswer2(), q.getWrongAnswer3())
                                .stream()
                                .sorted((a, b) -> ThreadLocalRandom.current().nextInt(-1, 2))
                                .toList(),
                        q.getRightAnswer(),
                        q.getImageUrl(),
                        q.getImageFilename(),
                        q.getJustificationImageName(),
                        q.getJustificationImageUrl()
                ))
                .collect(Collectors.toList());
    }
}