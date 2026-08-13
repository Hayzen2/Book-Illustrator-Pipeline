package com.example.BookIllustrator.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BookIllustrator.entity.Chapter;
import com.example.BookIllustrator.entity.Project;
import com.example.BookIllustrator.entity.ProjectStep;
import com.example.BookIllustrator.entity.StoryCharacter;
import com.example.BookIllustrator.enums.GlobalStatus;
import com.example.BookIllustrator.enums.StepName;
import com.example.BookIllustrator.enums.StepStatus;
import com.example.BookIllustrator.repository.ChapterRepository;
import com.example.BookIllustrator.repository.ProjectRepository;
import com.example.BookIllustrator.repository.ProjectStepRepository;
import com.example.BookIllustrator.repository.StoryCharacterRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectStepExecutionService {
    private final GeminiInteractionService geminiInteractionService;
    private final ProjectStepRepository projectStepRepository;
    private final ProjectRepository projectRepository;
    private final StoryCharacterRepository storyCharacterRepository;
    private final ChapterRepository chapterRepository;
    private final FileStorageService fileStorageService;
    
    
    public void beginProjectExecution(Project project) {
        // Initialize the steps for the project
        List<StepName> steps = List.of(
            StepName.STYLE, 
            StepName.CHARACTERS, 
            StepName.PORTRAITS, 
            StepName.CHAPTERS, 
            StepName.ILLUSTRATIONS
        );
        
        // Save each step to the database with PENDING status
        for (StepName name : steps) {
            ProjectStep step = new ProjectStep();
            step.setProject(project);
            step.setStepName(name);
            step.setStatus(StepStatus.PENDING);
            projectStepRepository.save(step);
        }
    }

    // initiate the execution chain for the project steps
    public void initializeContextChain(Project project) {
        // Call Gemini to take book_interaction_id (Context Chaining)
        String interactionId = geminiInteractionService.initializeGeminiInteraction(
            project.getId(), 
            project.getBookFilePath()
        );
        
        // Update database with the interaction ID
        project.setBookInteractionId(interactionId);
        projectRepository.save(project);
    }

    @Transactional
    public void executeStepAsync(Long projectId, StepName stepName, String customStyle) {
        // Atomic update -> IN_PROGRESS
        int updatedRows = projectStepRepository.claimStepForExecution(projectId, stepName);
        if (updatedRows == 0) {
            throw new IllegalStateException("Step is already running or cannot be executed right now.");
        }

        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));

        // Run background task (Fire and Forget)
        Long pid = project.getId();
        StepName sn = stepName;
        CompletableFuture.runAsync(() -> {
            Project p = projectRepository.findById(pid)
                .orElseThrow(() -> new RuntimeException("Project not found"));
            processExternalGeminiCall(p, sn, customStyle);
        }).exceptionally(ex -> {
            log.error("Unhandled exception in background step execution: ", ex);
            return null;
        });
    }
    private void updateGlobalStatus(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new RuntimeException("Project not found: " + projectId));

        List<ProjectStep> steps =
                projectStepRepository.findByProjectId(projectId);

        boolean hasCompletedStep = steps.stream()
                .anyMatch(step ->
                        step.getStatus() == StepStatus.COMPLETED);

        boolean allCompleted =
                !steps.isEmpty()
                && steps.stream()
                        .allMatch(step ->
                                step.getStatus() == StepStatus.COMPLETED);

        if (allCompleted) {

            project.setStatus(GlobalStatus.DONE);

        } else if (hasCompletedStep) {

            project.setStatus(GlobalStatus.IN_PROGRESS);

        } else {

            project.setStatus(GlobalStatus.DRAFT);
        }

        projectRepository.save(project);

        log.info(
                "Updated global status for project {} -> {}",
                projectId,
                project.getStatus()
        );
    }

    // Process the external Gemini call for the given step
    private void processExternalGeminiCall(Project project, StepName stepName, String customStyle) {
        try {
            final Long projectId = project.getId();
            String currentInteractionId = project.getBookInteractionId();

            switch (stepName) {
                case STYLE:
                    // Generate style for the book
                    Map<String, String> styleResult = geminiInteractionService.callGeminiStyle(customStyle, currentInteractionId);
                    if (styleResult.containsKey("interactionId")) {
                        project.setBookInteractionId(styleResult.get("interactionId"));
                        // Save the generated style text to the project entity
                        project.setArtStyle(styleResult.get("artStyle"));
                        projectRepository.save(project);
                        // refresh the currentInteractionId so subsequent steps use the updated context
                        currentInteractionId = project.getBookInteractionId();
                    }
                    break;
                case CHARACTERS:
                    // Generate characters for the book
                    Map<String, Object> charResult = geminiInteractionService.callGeminiCharacters(currentInteractionId);
                    if (charResult.containsKey("interactionId")) {
                        project.setBookInteractionId((String) charResult.get("interactionId"));
                        projectRepository.save(project);
                        // update currentInteractionId to the new one
                        currentInteractionId = project.getBookInteractionId();
                    }
                    // Save the extracted characters to the database
                    Object charactersObject = charResult.get("characters");
                    if (charactersObject instanceof List<?> extractedChars) {
                        for (Object charObject : extractedChars) {
                            if (charObject instanceof Map<?, ?> charData) {
                                Object name = charData.get("name");
                                Object prompt = charData.get("prompt");

                                if (name instanceof String && prompt instanceof String) {
                                    StoryCharacter character = new StoryCharacter();
                                    character.setProject(project);
                                    character.setName((String) name);
                                    character.setImagePrompt((String) prompt);
                                    storyCharacterRepository.save(character);
                                }
                            }
                        }
                    }
                    break;

                case PORTRAITS:
                    // Generate portrait images for the characters (Call the model for the first character)
                    // reload project to ensure we have the latest interaction id and style
                    project = projectRepository.findById(projectId)
                        .orElseThrow(() -> new RuntimeException("Project not found"));
                    currentInteractionId = project.getBookInteractionId();

                    List<StoryCharacter> characters = storyCharacterRepository.findByProjectId(projectId)
                        .orElseThrow(() -> new RuntimeException("No characters found for project with ID: " + projectId));
                    String styleText = project.getArtStyle() != null ? project.getArtStyle() : "";
                    
                    for (StoryCharacter character : characters) {
                         String imagePath = geminiInteractionService.callGeminiPortrait(
                            character.getName(), 
                            character.getImagePrompt(), 
                            styleText, 
                            currentInteractionId, 
                            projectId
                        );
                        // Save the generated image path back to the character record
                        character.setPortraitImagePath(imagePath);
                        storyCharacterRepository.save(character);
                    }
                    break;

                case CHAPTERS:
                    // Generate list of chapters (maximum 1 chapter)
                    Map<String, Object> chapterResult = geminiInteractionService.callGeminiChapters(currentInteractionId);
                    if (chapterResult.containsKey("interactionId")) {
                        project.setBookInteractionId((String) chapterResult.get("interactionId"));
                        projectRepository.save(project);
                        // update currentInteractionId after chapters generation
                        currentInteractionId = project.getBookInteractionId();
                    }
                    
                    // Save the extracted chapters to the database 
                    Object chaptersObj = chapterResult.get("chapters");
                    if (chaptersObj instanceof List<?> rawList) {
                        for (Object item : rawList) {
                            if (item instanceof Map<?, ?> rawMap) {
                                // Build a typed Map<String, Object> safely
                                Map<String, Object> chapData = new java.util.HashMap<>();
                                for (Map.Entry<?, ?> e : rawMap.entrySet()) {
                                    if (e.getKey() instanceof String key) {
                                        chapData.put(key, e.getValue());
                                    }
                                }

                                Chapter chapter = new Chapter();
                                chapter.setProject(project);
                                chapter.setName((String) chapData.get("name"));
                                chapter.setIllustrationPrompt((String) chapData.get("prompt"));

                                // Handle the optional 'characters' array: associate existing StoryCharacter entities
                                Object charsObj = chapData.get("characters");
                                if (charsObj instanceof List<?> charNames) {
                                    List<StoryCharacter> chapterCharacters = new ArrayList<>();
                                    for (Object cn : charNames) {
                                        if (cn instanceof String charName) {
                                            storyCharacterRepository.findByProjectIdAndName(projectId, charName)
                                                .ifPresent(chapterCharacters::add);
                                        }
                                    }
                                    chapter.setCharacters(chapterCharacters);
                                }

                                chapterRepository.save(chapter);
                            }
                        }
                    }
                    break;

                case ILLUSTRATIONS:
                    // Generate illustrations for the book (final scene with reference images)
                    // FETCH CHAPTERS AND PORTRAITS FROM DB
                    // reload project to get latest interaction id and style
                    project = projectRepository.findById(projectId)
                        .orElseThrow(() -> new RuntimeException("Project not found"));
                    currentInteractionId = project.getBookInteractionId();

                    List<Chapter> chapters = chapterRepository.findByProjectId(projectId)
                        .orElseThrow(() -> new RuntimeException("No chapters found for project with ID: " + projectId));
                    List<StoryCharacter> allCharacters = storyCharacterRepository.findByProjectId(projectId)
                        .orElseThrow(() -> new RuntimeException("No characters found for project with ID: " + projectId));
                    String currentStyle = project.getArtStyle() != null ? project.getArtStyle() : "";
                    
                    // Convert saved portraits to Base64 to pass as references
                    List<String> base64ReferenceImages = new ArrayList<>();
                    for (StoryCharacter c : allCharacters) {
                        if (c.getPortraitImagePath() != null) {
                            base64ReferenceImages.add(fileStorageService.readImageAsBase64(c.getPortraitImagePath()));
                        }
                    }

                    for (Chapter chapter : chapters) {
                        String illustrationPath = geminiInteractionService.callGeminiIllustration(
                            chapter.getName(), 
                            chapter.getIllustrationPrompt(), 
                            currentStyle, 
                            currentInteractionId, 
                            base64ReferenceImages, 
                            projectId
                        );
                        // Save the generated image path back to the chapter record
                        chapter.setIllustrationImagePath(illustrationPath);
                        chapterRepository.save(chapter);
                    }
                    break;

                default:
                    log.warn("Unknown step name: {}", stepName);
            }
            
            // Completed
            updateStepStatus(
                project.getId(),
                stepName,
                StepStatus.COMPLETED,
                null
            );
            updateStepStatus(project.getId(), stepName, StepStatus.COMPLETED, null);
            
        } catch (Exception e) {
            log.error("Error executing step {}: {}", stepName, e.getMessage());
            // Error -> FAILED + save error message to the database
            // Failed
            updateStepStatus(project.getId(), stepName, StepStatus.FAILED, e.getMessage());
        }
    }

    @Transactional
    public void updateStepStatus(Long projectId, StepName stepName, StepStatus status, String errorMessage) {
        projectStepRepository.findByProjectIdAndStepName(projectId, stepName).ifPresent(step -> {
            step.setStatus(status);
            step.setErrorMessage(errorMessage);
            projectStepRepository.save(step);
        });
    }
}
