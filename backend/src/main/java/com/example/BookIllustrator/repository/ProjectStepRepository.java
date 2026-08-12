package com.example.BookIllustrator.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.BookIllustrator.entity.ProjectStep;
import com.example.BookIllustrator.enums.StepName;

@Repository
public interface ProjectStepRepository extends JpaRepository<ProjectStep, Long> {
    Optional<ProjectStep> findByProjectIdAndStepName(Long projectId, StepName stepName);
    // Atomic Claim-Check: 
    // Can only change status from PENDING to IN_PROGRESS 
    // if the current status is PENDING or FAILED
    // Prevent double-click and F5 overwrite issues that make the step status inconsistents
    @Modifying
    @Query("""
        UPDATE ProjectStep s 
        SET s.status = 'IN_PROGRESS', s.errorMessage = null 
        WHERE s.project.id = :projectId 
          AND s.stepName = :stepName 
          AND s.status IN ('PENDING', 'FAILED')
    """)
    void updateStepStatusToInProgress(@Param("projectId") Long projectId, @Param("stepName") StepName stepName, @Param("errorMessage") String errorMessage);
}