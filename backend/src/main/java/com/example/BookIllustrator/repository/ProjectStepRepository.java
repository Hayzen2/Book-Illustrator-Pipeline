package com.example.BookIllustrator.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.BookIllustrator.entity.ProjectStep;

@Repository
public interface ProjectStepRepository extends JpaRepository<ProjectStep, Long> {
    
}
