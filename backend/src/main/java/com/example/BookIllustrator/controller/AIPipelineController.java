package com.example.BookIllustrator.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.BookIllustrator.dto.api.ApiResponse;
import com.example.BookIllustrator.dto.project.response.ProjectDetailResponse;
import com.example.BookIllustrator.enums.StepName;
import com.example.BookIllustrator.service.ProjectManagementService;
import com.example.BookIllustrator.service.ProjectStepExecutionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/projects/{projectId}/steps")
@RequiredArgsConstructor
public class AIPipelineController {
    private final ProjectStepExecutionService projectStepExecutionService;
    private final ProjectManagementService projectManagementService;

    @PostMapping("/{stepName}/execute")
    public ApiResponse<Void> executeProjectStep(
            @PathVariable Long projectId, 
            @PathVariable StepName stepName,
            @RequestBody(required = false) String customStyle,
            @AuthenticationPrincipal Long userId) {
        // Validate that the project belongs to the user
        projectManagementService.validateProjectOwnership(projectId, userId);
        // Execute the step asynchronously
        projectStepExecutionService.executeStepAsync(projectId, stepName, customStyle);
        
        return new ApiResponse<>(202, "Step execution started in background", null);
    }

    // FAT GET endpoint to fetch the status of the entire pipeline for a project
    // Returns the status of each step in the pipeline for the given project, error messages and images generated
    @GetMapping("/status")
    public ApiResponse<ProjectDetailResponse> getPipelineStatus(
            @PathVariable Long projectId,
            @AuthenticationPrincipal Long userId) {
        
        ProjectDetailResponse details = projectManagementService.getProjectDetails(projectId, userId);
        return new ApiResponse<>(200, "Pipeline status fetched successfully", details);
    }
}