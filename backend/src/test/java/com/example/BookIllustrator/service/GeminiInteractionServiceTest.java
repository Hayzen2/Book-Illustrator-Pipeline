package com.example.BookIllustrator.service;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class GeminiInteractionServiceTest {

    @Mock(answer = org.mockito.Answers.RETURNS_DEEP_STUBS)
    private WebClient geminiWebClient;

    @Mock
    private FileStorageService fileStorageService;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private GeminiInteractionService geminiInteractionService;

    @Test
    void initializeGeminiInteraction_returnsInteractionId(@org.junit.jupiter.api.io.TempDir Path tempDir) throws Exception {
        Path bookFile = tempDir.resolve("book.txt");
        Files.writeString(bookFile, "A short story.");

        when(geminiWebClient.post().uri("/interactions").contentType(any(MediaType.class)).bodyValue(any()).retrieve().bodyToMono(String.class))
            .thenReturn(Mono.just("{\"interactionId\":\"interaction-123\"}"));

        String interactionId = geminiInteractionService.initializeGeminiInteraction(1L, bookFile.toString());

        assertEquals("interaction-123", interactionId);
    }

    @Test
    void callGeminiStyle_returnsGeneratedStyleWhenCustomStyleBlank() {
        when(geminiWebClient.post().uri("/interactions").contentType(any(MediaType.class)).bodyValue(any()).retrieve().bodyToMono(String.class))
            .thenReturn(Mono.just("{\"interactionId\":\"interaction-456\",\"text\":\"Watercolor fantasy style\"}"));

        Map<String, String> result = geminiInteractionService.callGeminiStyle("", "previous-id");

        assertEquals("interaction-456", result.get("interactionId"));
        assertEquals("Watercolor fantasy style", result.get("artStyle"));
    }

    @Test
    void callGeminiStyle_prefersCustomStyleWhenProvided() {
        when(geminiWebClient.post().uri("/interactions").contentType(any(MediaType.class)).bodyValue(any()).retrieve().bodyToMono(String.class))
            .thenReturn(Mono.just("{\"interactionId\":\"interaction-789\",\"text\":\"ignored\"}"));

        Map<String, String> result = geminiInteractionService.callGeminiStyle("Ink wash", "previous-id");

        assertEquals("interaction-789", result.get("interactionId"));
        assertEquals("Ink wash", result.get("artStyle"));
    }

    @Test
    void callGeminiCharacters_parsesCharacters() {
        when(geminiWebClient.post().uri("/interactions").contentType(any(MediaType.class)).bodyValue(any()).retrieve().bodyToMono(String.class))
            .thenReturn(Mono.just("{\"interactionId\":\"interaction-111\",\"text\":\"[{\\\"name\\\":\\\"Milo\\\",\\\"prompt\\\":\\\"A brave explorer.\\\"}]\"}"));

        Map<String, Object> result = geminiInteractionService.callGeminiCharacters("previous-id");

        assertEquals("interaction-111", result.get("interactionId"));
        assertEquals(1, ((List<?>) result.get("characters")).size());
    }

    @Test
    void callGeminiPortrait_returnsSavedPath() {
        when(geminiWebClient.post().uri("/interactions").contentType(any(MediaType.class)).bodyValue(any()).retrieve().bodyToMono(String.class))
            .thenReturn(Mono.just("{\"candidates\":[{\"content\":{\"parts\":[{\"inlineData\":{\"data\":\"BASE64_IMAGE\"}}]}}]}"));
        when(fileStorageService.savePortraitToLocalStorage("Milo", "BASE64_IMAGE", 7L))
            .thenReturn("/uploads/mock_image.png");

        String path = geminiInteractionService.callGeminiPortrait("Milo", "prompt", "style", "previous-id", 7L);

        assertEquals("/uploads/mock_image.png", path);
    }

    @Test
    void callGeminiIllustration_returnsSavedPath() {
        when(geminiWebClient.post().uri("/interactions").contentType(any(MediaType.class)).bodyValue(any()).retrieve().bodyToMono(String.class))
            .thenReturn(Mono.just("{\"candidates\":[{\"content\":{\"parts\":[{\"inlineData\":{\"data\":\"BASE64_IMAGE\"}}]}}]}"));
        when(fileStorageService.saveIllustrationToLocalStorage("Chapter 1", "BASE64_IMAGE", 9L))
            .thenReturn("/uploads/mock_image.png");

        String path = geminiInteractionService.callGeminiIllustration("Chapter 1", "chapter prompt", "style", "previous-id", List.of("ref1"), 9L);

        assertEquals("/uploads/mock_image.png", path);
    }

    @Test
    void privateExtractBase64ImageFromResponse_fallsBackToFlatField() throws Exception {
        Method method = GeminiInteractionService.class.getDeclaredMethod("extractBase64ImageFromResponse", String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(geminiInteractionService, "{\"image_base64\":\"ABC\"}");

        assertEquals("ABC", result);
    }
}
