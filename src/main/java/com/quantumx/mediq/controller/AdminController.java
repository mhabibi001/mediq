package com.quantumx.mediq.controller;

import com.quantumx.mediq.model.Category;
import com.quantumx.mediq.model.Question;
import com.quantumx.mediq.repository.CategoryRepository;
import com.quantumx.mediq.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
        // Check if category is provided
        if (question.getCategory() == null || question.getCategory().getId() == null) {
            throw new IllegalArgumentException("Category is required");
        }

        // Fetch the category from the database to ensure it exists
        Category category = categoryRepository.findById(question.getCategory().getId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        // Assign the fetched category to the question
        question.setCategory(category);

        // Save and return the question
        return questionRepository.save(question);
    }

}
