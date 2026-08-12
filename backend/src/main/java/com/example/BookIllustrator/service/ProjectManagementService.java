package com.example.BookIllustrator.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.BookIllustrator.dto.project.ProjectCreateRequest;
import com.example.BookIllustrator.entity.Project;
import com.example.BookIllustrator.entity.User;
import com.example.BookIllustrator.repository.ProjectRepository;
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
}
