package com.example.BookIllustrator.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.BookIllustrator.dto.api.ApiResponse;
import com.example.BookIllustrator.dto.project.request.ProjectCreateRequest;
import com.example.BookIllustrator.dto.project.response.ProjectListResponse;
import com.example.BookIllustrator.entity.Project;
import com.example.BookIllustrator.repository.ProjectRepository;
import com.example.BookIllustrator.service.ProjectManagementService;
import com.example.BookIllustrator.service.ProjectStepExecutionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectManagementService projectManagementService;
    private final ProjectRepository projectRepository;
    private final ProjectStepExecutionService projectStepExecutionService;

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

    @PostMapping("/create")
    public ApiResponse<Project> createProject(@AuthenticationPrincipal Long userId, @RequestBody ProjectCreateRequest request) {
        Project project = projectManagementService.createProject(userId, request);
        projectStepExecutionService.initializeContextChain(project);
        return new ApiResponse<>(201, "Project created successfully", project);
    }

    // Endpoint to create a project with a file upload txt file
    @PostMapping(value = "/create/txt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Project> createProjectWithFile(
            @AuthenticationPrincipal Long userId,
            @RequestParam("title") String title,
            @RequestParam("file") MultipartFile fileUpload) {
        Project project = projectManagementService.createProject(userId, title, fileUpload);
        projectStepExecutionService.initializeContextChain(project);
        return new ApiResponse<>(201, "Project created successfully", project);
    }

    @DeleteMapping("/{projectId}")
    public ApiResponse<Void> deleteProject(@PathVariable Long projectId, @AuthenticationPrincipal Long userId) {
        Project project = projectRepository.findByIdAndUserId(projectId, userId).orElseThrow(() -> new RuntimeException("Project not found with ID: " + projectId));
        projectManagementService.deleteProject(project);
        return new ApiResponse<>(200, "Project deleted successfully", null);
    }
}
