package com.quantumx.mediq.controller;

import com.quantumx.mediq.model.EducationLevel;
import com.quantumx.mediq.model.ExamCategory;
import com.quantumx.mediq.model.TimeZone;
import com.quantumx.mediq.repository.EducationLevelRepository;
import com.quantumx.mediq.repository.ExamCategoryRepository;
import com.quantumx.mediq.repository.TimeZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RegistrationController {

    private final EducationLevelRepository educationLevelRepository;
    private final TimeZoneRepository timeZoneRepository;
    private final ExamCategoryRepository examCategoryRepository;

    /**
     * ✅ Fetch all available education levels
     */
    @GetMapping("/education-levels")
    public ResponseEntity<List<EducationLevel>> getAllEducationLevels() {
        return ResponseEntity.ok(educationLevelRepository.findAll());
    }

    /**
     * ✅ Fetch all available time zones
     */
    @GetMapping("/timezones")
    public ResponseEntity<List<TimeZone>> getAllTimeZones() {
        return ResponseEntity.ok(timeZoneRepository.findAll());
    }

    @GetMapping("/exam-categories")
    public ResponseEntity<List<ExamCategory>> getAllExamCategories() {
        List<ExamCategory> categories = examCategoryRepository.findAll();
        return ResponseEntity.ok(categories);
    }
}
