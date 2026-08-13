package com.example.BookIllustrator.service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.BookIllustrator.entity.Project;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileStorageService {
    private static final String STORAGE_DIRECTORY = "uploads/books";
    
    // Validate if the uploaded file is a text file
    public boolean isValidFileType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.equals("text/plain");
    }
    
    // Check if the storage directory exists
    public boolean doesStorageDirectoryExist() {
        Path storagePath = Paths.get(STORAGE_DIRECTORY);
        return Files.exists(storagePath) && Files.isDirectory(storagePath);
    }
    
    // Save book with uploaded file
    public String saveBookTXT(Long userId, String title, MultipartFile file) {
       try {
            // Create the directory path: storageDirectory/userId/
            Path dirPath = Paths.get(STORAGE_DIRECTORY, String.valueOf(userId));

            // Create the directory if it doesn't exist
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // Sanitize title to prevent invalid filename characters
            String sanitizedTitle = title.replaceAll("[^a-zA-Z0-9-_]", "_");

            // Create the file path: storageDirectory/userId/title.txt
            Path filePath = dirPath.resolve(sanitizedTitle + ".txt");
            // Save the file to the specified path
            file.transferTo(filePath.toFile());
            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save the file: " + e.getMessage());
        }
    }

    // Save book with text content
    public String saveBookTXT(Long userId, String title, String bookText) {
        try {
            // Create the directory path: storageDirectory/userId/
            Path dirPath = Paths.get(STORAGE_DIRECTORY, String.valueOf(userId));
            // Create the directory if it doesn't exist
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            // Sanitize title to prevent invalid filename characters
            String sanitizedTitle = title.replaceAll("[^a-zA-Z0-9-_]", "_");
            // Create the file path: storageDirectory/userId/title.txt
            Path filePath = dirPath.resolve(sanitizedTitle + ".txt");
            // Save the book text to the specified path
            Files.write(filePath, bookText.getBytes());
            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save the file: " + e.getMessage());
        }
    }

    public String savePortraitToLocalStorage(String characterName, String base64Image, Long projectId) {
        try {
            // Create the directory path: storageDirectory/projectId/
            Path dirPath = Paths.get(STORAGE_DIRECTORY, "portraits", String.valueOf(projectId));
            // Create the directory if it doesn't exist
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            // Sanitize character name to prevent invalid filename characters
            String sanitizedCharacterName = characterName.replaceAll("[^a-zA-Z0-9-_]", "_");
            // Create the file path: storageDirectory/projectId/characterName.png
            Path filePath = dirPath.resolve(sanitizedCharacterName + ".png");
            // Decode the base64 image and save it to the specified path
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Image);
            Files.write(filePath, imageBytes);
            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save the portrait: " + e.getMessage());
        }
    }

    // Save illustration to local storage
    public String saveIllustrationToLocalStorage(String chapterName, String base64Image, Long projectId) {
        try {
            // Create the directory path: storageDirectory/projectId/illustrations/
            Path dirPath = Paths.get(STORAGE_DIRECTORY, "illustrations", String.valueOf(projectId));
            // Create the directory if it doesn't exist
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            // Sanitize chapter name to prevent invalid filename characters
            String sanitizedChapterName = chapterName.replaceAll("[^a-zA-Z0-9-_]", "_");
            // Create the file path: storageDirectory/projectId/illustrations/chapterName.png
            Path filePath = dirPath.resolve(sanitizedChapterName + ".png");
            // Decode the base64 image and save it to the specified path
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Image);
            Files.write(filePath, imageBytes);
            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save the illustration: " + e.getMessage());
        }
    }

    // Read image from local storage and convert to Base64
    public String readImageAsBase64(String imagePath) {
        try {
            Path path = Paths.get(imagePath);
            byte[] imageBytes = Files.readAllBytes(path);
            return java.util.Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read the image: " + e.getMessage());
        }
    }

    public void deleteProjectFiles(Project project) {
        try {
            // Delete the book file
            Path bookFilePath = Paths.get(project.getBookFilePath());
            Files.deleteIfExists(bookFilePath);

            // Delete the portraits directory
            Path portraitsDirPath = Paths.get(STORAGE_DIRECTORY, "portraits", String.valueOf(project.getId()));
            if (Files.exists(portraitsDirPath)) {
                Files.walk(portraitsDirPath)
                    .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete file: " + path.toString(), e);
                        }
                    });
            }

            // Delete the illustrations directory
            Path illustrationsDirPath = Paths.get(STORAGE_DIRECTORY, "illustrations", String.valueOf(project.getId()));
            if (Files.exists(illustrationsDirPath)) {
                Files.walk(illustrationsDirPath)
                    .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete file: " + path.toString(), e);
                        }
                    });
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete project files: " + e.getMessage(), e);
        }
    }
}