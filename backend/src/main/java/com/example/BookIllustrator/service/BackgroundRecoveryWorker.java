package com.example.BookIllustrator.service;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.BookIllustrator.entity.ProjectStep;
import com.example.BookIllustrator.enums.StepStatus;
import com.example.BookIllustrator.repository.ProjectStepRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BackgroundRecoveryWorker {
    private final ProjectStepRepository projectStepRepository;
    private final ProjectStepExecutionService projectStepExecutionService;

    // This method runs every minute to check for stuck steps and mark them as 
    // FAILED if they have been in PENDING or IN_PROGRESS for too long (5 minutes).
    @Scheduled(fixedDelay = 60000)
    public void retryStuckSteps() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(5);
        List<ProjectStep> stuckSteps = projectStepRepository.findStuckSteps(timeoutThreshold);

        if (!stuckSteps.isEmpty()) {
            log.warn("Found {} stuck steps. Marking them as FAILED.", stuckSteps.size());

            for (ProjectStep step : stuckSteps) {
                projectStepExecutionService.updateStepStatus(
                    step.getProject().getId(), 
                    step.getStepName(), 
                    StepStatus.FAILED, 
                    "System interrupted unexpectedly (Timeout). Please retry."
                );
            }
        }
    }
}
