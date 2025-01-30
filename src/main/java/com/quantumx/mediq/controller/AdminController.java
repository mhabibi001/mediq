package com.quantumx.mediq.controller;

import com.quantumx.mediq.model.Category;
import com.quantumx.mediq.model.Question;
import com.quantumx.mediq.repository.CategoryRepository;
import com.quantumx.mediq.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/categories")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll(); // Fetch all categories from the database
    }

    @PostMapping("/add-question")
    public Question addQuestion(@RequestBody Question question) {
        if (question.getCategory() == null || question.getCategory().getId() == null) {
            throw new IllegalArgumentException("Category is required");
        }

        // Ensure the category exists in the database
        Category category = categoryRepository.findById(question.getCategory().getId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        // Assign the category to the question
        question.setCategory(category);

        return questionRepository.save(question);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(IllegalArgumentException ex) {
        return ex.getMessage();
    }
}
