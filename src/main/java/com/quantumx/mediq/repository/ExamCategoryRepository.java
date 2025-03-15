package com.quantumx.mediq.repository;

import com.quantumx.mediq.model.ExamCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamCategoryRepository extends JpaRepository<ExamCategory, Long> {
    List<ExamCategory> findAll();
}
