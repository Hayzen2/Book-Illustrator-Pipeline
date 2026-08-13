package com.example.BookIllustrator.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.BookIllustrator.dto.project.request.ProjectCreateRequest;
import com.example.BookIllustrator.dto.project.response.ChapterDetailsResponse;
import com.example.BookIllustrator.dto.project.response.CharacterDetailsResponse;
import com.example.BookIllustrator.dto.project.response.ProjectDetailResponse;
import com.example.BookIllustrator.dto.project.response.ProjectStepResponse;
import com.example.BookIllustrator.entity.Project;
import com.example.BookIllustrator.entity.User;
import com.example.BookIllustrator.repository.ChapterRepository;
import com.example.BookIllustrator.repository.ProjectRepository;
import com.example.BookIllustrator.repository.ProjectStepRepository;
import com.example.BookIllustrator.repository.StoryCharacterRepository;
import com.example.BookIllustrator.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectManagementService {
    private final ProjectStepExecutionService projectStepExecutionService;
    private final ProjectRepository projectRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final ProjectStepRepository projectStepRepository;
    private final StoryCharacterRepository storyCharacterRepository;
    private final ChapterRepository chapterRepository;

    @Transactional // All-or-nothing
    public Project createProject(Long userId, ProjectCreateRequest request) {
        // Save to file storage and get the file path (txt file)
        String filePath = fileStorageService.saveBookTXT(userId, request.getTitle(), request.getBookText());
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        Project project = new Project();
        project.setUser(user);
        project.setTitle(request.getTitle());
        project.setBookFilePath(filePath);

        // Save the project to the database
        project = projectRepository.save(project);
        projectStepExecutionService.beginProjectExecution(project); // Execute project steps after saving

        return project;
    }

    @Transactional // All-or-nothing
    public Project createProject(Long userId, String title, MultipartFile file) {
        // Save to file storage and get the file path (txt file)
        String filePath = fileStorageService.saveBookTXT(userId, title, file);
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        Project project = new Project();
        project.setUser(user);
        project.setTitle(title);
        project.setBookFilePath(filePath);

        // Save the project to the database
        project = projectRepository.save(project);
        projectStepExecutionService.beginProjectExecution(project); // Execute project steps after saving

        return project;
    }

    // Validate that the project belongs to the user
    public void validateProjectOwnership(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found with ID: " + projectId));
        if (!project.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to project with ID: " + projectId);
        }
    }

    @Transactional
    public ProjectDetailResponse getProjectDetails(Long projectId, Long userId) {
        Project project = projectRepository.findByIdAndUserId(projectId, userId)
            .orElseThrow(() -> new RuntimeException("Project not found or unauthorized"));

        // Fetch the steps, characters, and chapters associated with the project
        List<ProjectStepResponse> steps = projectStepRepository.findByProjectId(projectId).stream()
            .map(step -> new ProjectStepResponse(
                step.getStepName(), 
                step.getStatus(), 
                step.getErrorMessage(), 
                step.getUpdatedAt()))
            .toList();

        // Fetch characters and chapters associated with the project
        List<CharacterDetailsResponse> characters = storyCharacterRepository.findByProjectId(projectId)
            .orElse(List.of()).stream()
            .map(c -> new CharacterDetailsResponse(c.getId(), c.getName(), c.getImagePrompt(), c.getPortraitImagePath()))
            .toList();

        // Fetch chapters associated with the project
        List<ChapterDetailsResponse> chapters = chapterRepository.findByProjectId(projectId)
            .orElse(List.of()).stream()
            .map(c -> new ChapterDetailsResponse(c.getId(), c.getName(), c.getIllustrationPrompt(), c.getIllustrationImagePath()))
            .toList();

        // Return the project details response
        return new ProjectDetailResponse(
            project.getId(),
            project.getTitle(),
            project.getArtStyle(),
            project.getStatus(),
            steps,
            characters,
            chapters
        );
    }
}
