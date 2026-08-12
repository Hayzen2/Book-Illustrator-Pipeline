package com.example.BookIllustrator.service;

import org.springframework.stereotype.Service;

import com.example.BookIllustrator.entity.Project;
import com.example.BookIllustrator.repository.ProjectStepRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectStepExecutionService {
    private final GeminiInteractionService geminiInteractionService;
    private final ProjectStepRepository projectStepRepository;
    
    public void beginProjectExecution(Project project) {
        // Implementation for beginning project execution
    }

    public String executeStep(Long stepId) {
        // Implementation for executing a specific step
        return "Step executed successfully";
    }
}
