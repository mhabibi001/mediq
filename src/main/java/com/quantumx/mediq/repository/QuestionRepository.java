package com.quantumx.mediq.repository;

import com.quantumx.mediq.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByCategoryId(Long categoryId); // Get questions by category
    int countByCategoryId(Long categoryId); // Get total count of questions by category
}
