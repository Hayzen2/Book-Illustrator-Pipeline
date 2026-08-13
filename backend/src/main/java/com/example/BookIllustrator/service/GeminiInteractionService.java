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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
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
            return "Error initializing Gemini interaction: " + e.getMessage();
        }
    }

    private String executeGeminiRequest(Map<String, Object> requestBody) {
        try {
            String resp = geminiWebClient.post()
                    .uri("/interactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return resp == null ? "" : resp;
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
            "input", List.of(
                Map.of("type", "text", "text", inputPrompt)
            )
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
                Can you describe the main characters (only the adults and max 2 characters per book) 
                and prepare a prompt describing them with as much details as possible 
                (use the descriptions from the book) so Nano Banana can generate 
                images of them? Each prompt should be at least 50 words.
            """, 
            "response_format", Map.of(
                "type", "json",
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
                "type", "json",
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

    private String extractBase64ImageFromResponse(String responseJson) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(responseJson, new TypeReference<>() {});
            
            // Navigate the typical Gemini nested structure (adjust if your endpoint differs)
            // Example: { "candidates": [ { "content": { "parts": [ { "inlineData": { "data": "base64..." } } ] } } ] }
            if (responseMap.containsKey("candidates")) {
                 List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");
                 if (!candidates.isEmpty()) {
                     Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                     List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                     if (!parts.isEmpty() && parts.get(0).containsKey("inlineData")) {
                         Map<String, Object> inlineData = (Map<String, Object>) parts.get(0).get("inlineData");
                         return (String) inlineData.get("data");
                     }
                 }
            }
            // Fallback for flat structure
            return (String) responseMap.getOrDefault("image_base64", "");
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract base64 image from Gemini response: " + e.getMessage());
        }
    }

    private String extractTextFromResponse(String responseJson) {
        String text = ""; // Initialize to an empty string
        try {
            Map<String, Object> responseMap = objectMapper.readValue(responseJson, new TypeReference<>() {});
            // Try common locations for textual/json output in Gemini responses
            if (responseMap.containsKey("text") && responseMap.get("text") instanceof String) {
                text = (String) responseMap.get("text");
                return text;
            }

            if (responseMap.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");
                for (Map<String, Object> cand : candidates) {
                    Object contentObj = cand.get("content");
                    if (contentObj instanceof Map) {
                        Map<String, Object> content = (Map<String, Object>) contentObj;
                        Object partsObj = content.get("parts");
                        if (partsObj instanceof List) {
                            List<Map<String, Object>> parts = (List<Map<String, Object>>) partsObj;
                            for (Map<String, Object> part : parts) {
                                // part may contain 'text' or 'mime_type' + 'text' or 'inlineText'
                                if (part.containsKey("text") && part.get("text") instanceof String) {
                                    String candidate = (String) part.get("text");
                                    if (looksLikeJson(candidate)) return candidate;
                                }
                                if (part.containsKey("inlineText") && part.get("inlineText") instanceof String) {
                                    String candidate = (String) part.get("inlineText");
                                    if (looksLikeJson(candidate)) return candidate;
                                }
                            }
                        }
                    }
                }
            }

            // Last resort: search top-level values for a JSON-like string
            for (Object v : responseMap.values()) {
                if (v instanceof String) {
                    String s = (String) v;
                    if (looksLikeJson(s)) return s;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract text from Gemini response: " + e.getMessage());
        }
        return text;
    }

    private boolean looksLikeJson(String s) {
        if (s == null) return false;
        String trimmed = s.trim();
        return trimmed.startsWith("{") || trimmed.startsWith("[");
    }

    private String extractInteractionIdFromResponse(String responseJson) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(responseJson, new TypeReference<>() {});
            // try common keys
            if (responseMap.containsKey("interactionId")) return String.valueOf(responseMap.get("interactionId"));
            if (responseMap.containsKey("id")) return String.valueOf(responseMap.get("id"));
            if (responseMap.containsKey("name")) return String.valueOf(responseMap.get("name"));
            // fallback: return whole json (caller should handle)
            return responseJson;
        } catch (Exception e) {
            return responseJson;
        }
    }
}
