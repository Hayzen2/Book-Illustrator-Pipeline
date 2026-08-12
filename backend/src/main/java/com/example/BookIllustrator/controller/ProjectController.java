package com.example.BookIllustrator.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.BookIllustrator.dto.api.ApiResponse;
import com.example.BookIllustrator.dto.project.ProjectListResponse;
import com.example.BookIllustrator.entity.Project;
import com.example.BookIllustrator.repository.ProjectRepository;
import com.example.BookIllustrator.service.FileStorageService;
import com.example.BookIllustrator.service.ProjectStepExecutionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final FileStorageService fileStorageService;
    private final ProjectStepExecutionService projectService;
    private final ProjectRepository projectRepository;

    @GetMapping("/all")
    public ApiResponse<List<ProjectListResponse>> getProjectsByUserId(@AuthenticationPrincipal Long userId) {
        List<Project> projects = projectRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("No projects found for user with ID: " + userId));
        List<ProjectListResponse> projectResponses = projects.stream()
                .map(project -> new ProjectListResponse(
                        project.getId(),
                        project.getTitle(),
                        project.getStatus(),
                        project.getCreatedAt()
                ))
                .collect(Collectors.toList());
        return new ApiResponse<>(200, "Projects fetched successfully", projectResponses);
    }

    @GetMapping("/{projectId}")
    public ApiResponse<Project> getProjectById(@PathVariable Long projectId, @AuthenticationPrincipal Long userId) {
        Project project = projectRepository.findByIdAndUserId(projectId, userId).orElseThrow(() -> new RuntimeException("Project not found with ID: " + projectId));
        return new ApiResponse<>(200, "Project fetched successfully", project);
    }
}
