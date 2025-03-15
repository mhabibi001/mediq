package com.quantumx.mediq.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.quantumx.mediq.model.Category;
import com.quantumx.mediq.repository.CategoryRepository;

import java.util.List;

@RestController
@RequestMapping("/api/categories") // ✅ This ensures frontend calls are mapped correctly
@CrossOrigin(origins = "http://localhost:3000")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        try {
            List<Category> categories = categoryRepository.findAll();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}

