package com.example.BookIllustrator.service;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class ProjectStepExecutionServiceTest {

    @Mock
    private GeminiInteractionService geminiInteractionService;

    @Mock
    private ProjectStepRepository projectStepRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private StoryCharacterRepository storyCharacterRepository;

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ProjectStepExecutionService service;

    private Project project;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(1L);
        project.setTitle("Test Project");
        project.setBookFilePath("/tmp/book.txt");
        project.setBookInteractionId("interaction-1");
        project.setArtStyle("Watercolor");
        project.setStatus(GlobalStatus.DRAFT);
    }

    @Test
    void beginProjectExecution_savesAllFiveSteps() {
        service.beginProjectExecution(project);

        verify(projectStepRepository, times(5)).save(any(ProjectStep.class));
    }

    @Test
    void initializeContextChain_savesInteractionIdToProject() {
        when(geminiInteractionService.initializeGeminiInteraction(1L, "/tmp/book.txt")).thenReturn("interaction-xyz");
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.initializeContextChain(project);

        assertEquals("interaction-xyz", project.getBookInteractionId());
        verify(projectRepository).save(project);
    }

    @Test
    void executeStepAsync_throwsWhenClaimFails() {
        when(projectStepRepository.claimStepForExecution(1L, StepName.STYLE)).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> service.executeStepAsync(1L, StepName.STYLE, ""));
    }

    @Test
    void processStyleStep_updatesProjectStyleAndInteractionId() throws Exception {
        when(geminiInteractionService.callGeminiStyle("", "interaction-1"))
            .thenReturn(Map.of("interactionId", "interaction-2", "artStyle", "Soft watercolor"));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        invokeProcessExternalGeminiCall(project, StepName.STYLE, "");

        assertEquals("interaction-2", project.getBookInteractionId());
        assertEquals("Soft watercolor", project.getArtStyle());
    }

    @Test
    void processCharactersStep_savesStoryCharacters() throws Exception {
        when(geminiInteractionService.callGeminiCharacters("interaction-1")).thenReturn(Map.of(
            "interactionId", "interaction-2",
            "characters", List.of(Map.of("name", "Milo", "prompt", "A brave explorer."))
        ));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(storyCharacterRepository.save(any(StoryCharacter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        invokeProcessExternalGeminiCall(project, StepName.CHARACTERS, "");

        verify(storyCharacterRepository).save(any(StoryCharacter.class));
        assertEquals("interaction-2", project.getBookInteractionId());
    }

    @Test
    void processPortraitsStep_usesMockedPortraitPath() throws Exception {
        StoryCharacter character = new StoryCharacter();
        character.setId(10L);
        character.setName("Milo");
        character.setImagePrompt("Prompt");
        character.setProject(project);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(storyCharacterRepository.findByProjectId(1L)).thenReturn(Optional.of(List.of(character)));
        when(geminiInteractionService.callGeminiPortrait(anyString(), anyString(), anyString(), anyString(), anyLong()))
            .thenReturn("/uploads/mock_image.png");
        when(storyCharacterRepository.save(any(StoryCharacter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        invokeProcessExternalGeminiCall(project, StepName.PORTRAITS, "");

        assertEquals("/uploads/mock_image.png", character.getPortraitImagePath());
    }

    @Test
    void processChaptersStep_savesChapterCharacterLinks() throws Exception {
        when(geminiInteractionService.callGeminiForChapters("interaction-1")).thenReturn(Map.of(
            "interactionId", "interaction-2",
            "chapters", List.of(Map.of(
                "name", "Chapter 1",
                "prompt", "A dramatic chapter.",
                "characters", List.of("Milo")
            ))
        ));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        StoryCharacter character = new StoryCharacter();
        character.setName("Milo");
        when(storyCharacterRepository.findByProjectIdAndName(1L, "Milo")).thenReturn(Optional.of(character));
        when(chapterRepository.save(any(Chapter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        invokeProcessExternalGeminiCall(project, StepName.CHAPTERS, "");

        verify(chapterRepository).save(any(Chapter.class));
        assertEquals("interaction-2", project.getBookInteractionId());
    }

    @Test
    void processIllustrationsStep_usesMockedIllustrationPath() throws Exception {
        Chapter chapter = new Chapter();
        chapter.setId(20L);
        chapter.setName("Chapter 1");
        chapter.setIllustrationPrompt("A scene.");
        chapter.setProject(project);

        StoryCharacter character = new StoryCharacter();
        character.setName("Milo");
        character.setPortraitImagePath("/uploads/ref.png");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(chapterRepository.findByProjectId(1L)).thenReturn(Optional.of(List.of(chapter)));
        when(storyCharacterRepository.findByProjectId(1L)).thenReturn(Optional.of(List.of(character)));
        when(fileStorageService.readImageAsBase64("/uploads/ref.png")).thenReturn("BASE64_REF");
        when(geminiInteractionService.callGeminiIllustration(anyString(), anyString(), anyString(), anyString(), anyList(), anyLong()))
            .thenReturn("/uploads/mock_image.png");
        when(chapterRepository.save(any(Chapter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        invokeProcessExternalGeminiCall(project, StepName.ILLUSTRATIONS, "");

        assertEquals("/uploads/mock_image.png", chapter.getIllustrationImagePath());
    }

    @Test
    void updateStepStatus_savesStatusAndError() {
        ProjectStep step = new ProjectStep();
        when(projectStepRepository.findByProjectIdAndStepName(1L, StepName.STYLE)).thenReturn(Optional.of(step));
        when(projectStepRepository.save(any(ProjectStep.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateStepStatus(1L, StepName.STYLE, StepStatus.COMPLETED, null);

        assertEquals(StepStatus.COMPLETED, step.getStatus());
        assertEquals(null, step.getErrorMessage());
    }

    private void invokeProcessExternalGeminiCall(Project targetProject, StepName stepName, String customStyle) throws Exception {
        Method method = ProjectStepExecutionService.class.getDeclaredMethod(
            "processExternalGeminiCall",
            Project.class,
            StepName.class,
            String.class
        );
        method.setAccessible(true);
        method.invoke(service, targetProject, stepName, customStyle);
    }
}
