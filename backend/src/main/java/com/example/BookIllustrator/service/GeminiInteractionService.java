package com.example.BookIllustrator.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiInteractionService {
    @Value("${app.gemini.api-key}")
    private String apiKey;

    private final ObjectMapper objectMapper;
    private final WebClient geminiWebClient;
    private final FileStorageService fileStorageService;

    private static final String TEXT_MODEL = "gemini-2.5-flash";
    private static final String IMAGE_MODEL = "gemini-2.5-flash-image"; // Nano Banana
    private static final String SYSTEM_INSTRUCTIONS = """
        There must be no text on the image, it should not look like a cover page. 
        It should be an full illustration with no borders, titles, nor description. 
        Unless asked otherwise, stay family-friendly with uplifting colors. Each produced 
        should be a simple image, no panels.
    """;
    
    public String initializeGeminiInteraction(Long projectId, String filePath) {
        try {
            String bookContent = Files.readString(Path.of(filePath));
            // Prepare the request body for Gemini API
            Map<String, Object> requestBody = Map.of(
                "model", TEXT_MODEL,
                // System instructions for the model
                "input", List.of(
                    Map.of("type", "text", "text", 
                    """
                        Here's a book to illustrate. Don't say anything
                        yet, instructions will follow.
                    """
                    ),
                    Map.of("type", "text", "text", bookContent)
                )
            );
            String responseJson = executeGeminiRequest(requestBody);
            return extractInteractionIdFromResponse(responseJson);

        } catch (Exception e) {
            // Handle exceptions and log errors
            log.error("Failed to initialize Gemini interaction for project {}", projectId, e);
            throw new RuntimeException("Init failed: " + e.getMessage(), e);
        }
    }

    private String executeGeminiRequest(Map<String, Object> requestBody) {
    try {
            String resp = geminiWebClient.post()
                    .uri("/interactions")
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return resp == null ? "" : resp;
        } catch (WebClientResponseException e) {
            // This extracts the ACTUAL JSON error message sent back by Google
            String responseBody = e.getResponseBodyAsString();
            log.error("Gemini API Error Body: {}", responseBody);
            throw new RuntimeException("Gemini API Error: " + responseBody, e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    public Map<String, String> callGeminiStyle(String customStyle, String previousInteractionId) {
        // Implementation for calling Gemini API for style transfer
        String inputPrompt;
        if (customStyle == null || customStyle.isBlank()) {
            inputPrompt = 
            """
                Can you define a art style that would fit the story but with a twist? 
                Just give us the prompt for the art syle that will added to the furture prompts.
            """; 
        } else {
            inputPrompt = 
            "The art style will be: " + customStyle + ". Keep that in mind "
                + "when generating future prompts. Keep quiet for now, instructions will follow.";
        }

        Map<String, Object> requestBody = Map.of(
            "model", TEXT_MODEL,
            "previous_interaction_id", previousInteractionId,
            "input", inputPrompt
        );

        String responseJson = executeGeminiRequest(requestBody);
        String newInteractionId = extractInteractionIdFromResponse(responseJson);

        // Try to extract the style text from the response; if the caller provided a customStyle,
        // prefer it, otherwise use the model's generated text (if any).
        String generatedText = extractTextFromResponse(responseJson).trim();
        String artStyle = (customStyle != null && !customStyle.isBlank()) ? customStyle : (generatedText.isEmpty() ? "" : generatedText);

        return Map.of(
            "interactionId", newInteractionId,
            "artStyle", artStyle
        );
    }
    // NOTE: Limit 2 characters / book
    public Map<String, Object> callGeminiCharacters(String previousInteractionId) {
        // Schema for the expected JSON response from Gemini API
        Map<String, Object> schema = Map.of(
            "type", "array",
            "items", Map.of(
                "type",
                "object",
                "properties", Map.of(
                    "name", Map.of("type", "string"),
                    "prompt", Map.of("type", "string")
                ),
                "required", List.of("name", "prompt")
            )
        );
    
        // Prepare the request body for Gemini API to extract character details
        Map<String, Object> requestBody = Map.of(
            "model", TEXT_MODEL,
            "previous_interaction_id", previousInteractionId,
            "input", """
                Return ONLY a valid JSON array of the main adult characters from the book.
                Maximum 2 characters.
                Do not invent characters.
                Only include adults explicitly present in the book.
                If fewer than 2 exist, return fewer.
                If none exist, return [].
                Each object MUST contain:
                - name
                - prompt
                The prompt must be at least 50 words and describe the character using details supported by the book, suitable for image generation.
                NO explanation.
                NO Markdown.
                NO ```json.
                NO text before or after the JSON.
                Example:
                [
                {
                    "name": "Elias Vale",
                    "prompt": "..."
                }
                ]
            """, 
            "response_format", Map.of(
                "type", "text",
                "mime_type", "application/json",
                "schema", schema
            )
        );

        String responseJson = executeGeminiRequest(requestBody);
        // Parse the JSON response to extract character details
        try {
            String jsonText = extractTextFromResponse(responseJson);
            List<Map<String, String>> characters = objectMapper.readValue(jsonText, new TypeReference<>() {});
            String newId = extractInteractionIdFromResponse(responseJson);
            return Map.of(
                "interactionId", newId,
                "characters", characters
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse characters JSON from Gemini response: " + e.getMessage(), e);
        }
    }


    // NOTE: 1 portrait / character
    public String callGeminiPortrait(String characterName, String characterPrompt, String styleText, String previousInteractionId, Long projectId) {
        String combinedPrompt = "Create an illustration for " 
        + characterName 
        + " following this description: " 
        + characterPrompt; 
        
        Map<String, Object> requestBody = Map.of(
            "model", IMAGE_MODEL,
            "previous_interaction_id", previousInteractionId,
            "input", combinedPrompt 
            + ". The style we want you to follow is: " 
            + styleText + ". Also follow those rules: " 
            + SYSTEM_INSTRUCTIONS 
        );

        String responseJson = executeGeminiRequest(requestBody);
        // Parse the JSON response to extract the image data (base64 encoded)
        String base64Image = extractBase64ImageFromResponse(responseJson);
        // Save the image to local storage and return the file path
        return fileStorageService.savePortraitToLocalStorage(characterName, base64Image, projectId);
    }

    // NOTE: Limit 1 chapter / book
    public Map<String, Object> callGeminiForChapters(String previousInteractionId) {
        Map<String, Object> schema = Map.of(
            "type", "array",
            "items", Map.of(
                "type", "object",
                "properties", Map.of(
                    "name", Map.of("type", "string"),
                    "prompt", Map.of("type", "string"),
                    "characters", Map.of("type", "array", "items", Map.of("type", "string")) 
                ),
                "required", List.of("name", "prompt", "characters")
            )
        );

        Map<String, Object> requestBody = Map.of(
            "model", TEXT_MODEL,
            "previous_interaction_id", previousInteractionId,
            "input", """
                Now, for each chapters of the book (max 1 chapter per book), give me a prompt to illustrate what happens in it. 
                It should be a single image, not a multi-tiled page. Be very descriptive, especially 
                of the characters. Be very descriptive and remember to tell their name and to reuse 
                the character prompts if they appear in the images. Also list all characters who appear 
                in it.
            """,
            "response_format", Map.of(
                "type", "text",
                "mime_type", "application/json",
                "schema", schema
            )
        );

        String responseJson = executeGeminiRequest(requestBody);
        try {
            // Parse structured JSON from the response body text
            String jsonText = extractTextFromResponse(responseJson);
            List<Map<String, Object>> chapters = objectMapper.readValue(jsonText, new TypeReference<>() {});
            List<Map<String, Object>> cappedChapters = chapters.stream().limit(1).collect(Collectors.toList());
            String newId = extractInteractionIdFromResponse(responseJson);

            return Map.of(
                "interactionId", newId,
                "chapters", cappedChapters
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse chapters JSON from Gemini response: " + e.getMessage(), e);
        }
    }

    // NOTE: 1 illustration / chapter
    public String callGeminiIllustration(String chapterName, String chapterPrompt, String styleText, String previousInteractionId, List<String> base64ReferenceImages, Long projectId) {
        String inputPrompt = "Create this illustration for " + chapterName + ": " + chapterPrompt + " Use the provided images as references of what the characters look like."; 
        
        List<Object> inputParts = new ArrayList<>();
        inputParts.add(Map.of("type", "text", "text", inputPrompt + ". The style we want you to follow is: " + styleText + ". Also follow those rules: " + SYSTEM_INSTRUCTIONS));
        
        // Inject character portraits as references[cite: 2]
        if (base64ReferenceImages != null) {
            for (String base64Img : base64ReferenceImages) {
                inputParts.add(Map.of("type", "image", "data", base64Img, "mime_type", "image/png"));
            }
        }

        Map<String, Object> requestBody = Map.of(
            "model", IMAGE_MODEL,
            "previous_interaction_id", previousInteractionId,
            "input", inputParts 
        );
        
        String responseJson = executeGeminiRequest(requestBody);
        String base64Image = extractBase64ImageFromResponse(responseJson);
        return fileStorageService.saveIllustrationToLocalStorage(chapterName, base64Image, projectId);
    }

    private String extractTextFromResponse(String responseJson) {
        try {
            Map<String, Object> responseMap =
                    objectMapper.readValue(responseJson, new TypeReference<>() {});

            Object stepsObject = responseMap.get("steps");

            if (stepsObject instanceof List<?> steps) {
                for (int i = steps.size() - 1; i >= 0; i--) {
                    Object stepObject = steps.get(i);

                    if (!(stepObject instanceof Map<?, ?> step)) {
                        continue;
                    }

                    if (!"model_output".equals(step.get("type"))) {
                        continue;
                    }

                    Object contentObject = step.get("content");

                    if (!(contentObject instanceof List<?> contents)) {
                        continue;
                    }

                    for (int j = contents.size() - 1; j >= 0; j--) {
                        Object contentObjectItem = contents.get(j);

                        if (!(contentObjectItem instanceof Map<?, ?> content)) {
                            continue;
                        }

                        if ("text".equals(content.get("type"))
                                && content.get("text") instanceof String text) {

                            return cleanMarkdown(text);
                        }
                    }
                }
            }

            // Fallback
            if (responseMap.get("text") instanceof String text) {
                return cleanMarkdown(text);
            }

            log.warn("Could not find model output text in Gemini response: {}", responseJson);
            return "";

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to extract text from Gemini response: " + e.getMessage(), e);
        }
    }

    private String extractBase64ImageFromResponse(String responseJson) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(responseJson, new TypeReference<>() {});
            
            // Navigate the Interactions API structure to find the image
            if (responseMap.containsKey("steps")) {
                List<Map<String, Object>> steps = (List<Map<String, Object>>) responseMap.get("steps");
                if (steps != null && !steps.isEmpty()) {
                    // Iterate backwards to find the last model_output with an image
                    for (int i = steps.size() - 1; i >= 0; i--) {
                        Map<String, Object> step = steps.get(i);
                        if ("model_output".equals(step.get("type")) && step.containsKey("content")) {
                            List<Map<String, Object>> contents = (List<Map<String, Object>>) step.get("content");
                            for (Map<String, Object> content : contents) {
                                if ("image".equals(content.get("type")) && content.containsKey("data")) {
                                    return (String) content.get("data");
                                }
                            }
                        }
                    }
                }
            }
            
            // Fallback for flat structure
            return (String) responseMap.getOrDefault("image_base64", "");
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract base64 image from Gemini response: " + e.getMessage());
        }
    }

    // Helper method to strip markdown backticks from Gemini's JSON output
    private String cleanMarkdown(String rawText) {
        if (rawText == null) return "";
        String text = rawText.trim();
        if (text.startsWith("```json")) {
            text = text.substring(7);
        } else if (text.startsWith("```")) {
            text = text.substring(3);
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }
        return text.trim();
    }

    private String extractInteractionIdFromResponse(String responseJson) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(responseJson, new TypeReference<>() {});
            
            // Thêm key snake_case phổ biến của các API
            if (responseMap.containsKey("interaction_id")) return String.valueOf(responseMap.get("interaction_id"));
            if (responseMap.containsKey("interactionId")) return String.valueOf(responseMap.get("interactionId"));
            if (responseMap.containsKey("id")) return String.valueOf(responseMap.get("id"));
            if (responseMap.containsKey("name")) return String.valueOf(responseMap.get("name"));
            
            // Nếu không tìm thấy, NÉM LỖI RÕ RÀNG để dễ debug
            log.error("Could not find interaction ID in response: {}", responseJson);
            throw new RuntimeException("Missing interaction ID in API response");
            
        } catch (JsonProcessingException e) {
            log.error("Failed to parse interaction ID from: {}", responseJson);
            throw new RuntimeException("Invalid JSON response from API", e);
        }
    }
}
