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

    // NOTE: Limit 1 chapter / book
    public Map<String, Object> callGeminiChapters(String previousInteractionId) {

        Map<String, Object> schema = Map.of(
            "type", "array",
            "items", Map.of(
                "type", "object",
                "properties", Map.of(
                    "name", Map.of(
                        "type", "string"
                    ),
                    "prompt", Map.of(
                        "type", "string"
                    ),
                    "characters", Map.of(
                        "type", "array",
                        "items", Map.of(
                            "type", "string"
                        )
                    )
                ),
                "required", List.of(
                    "name",
                    "prompt",
                    "characters"
                )
            )
        );

        String inputPrompt = """
            Create ONE chapter illustration for this book.

            Choose the single most important or visually interesting chapter or scene.

            Return ONLY a valid JSON array containing exactly one object.

            Do not invent characters.
            Only use characters already identified in the previous step.

            The "name" field must contain the chapter name or a short scene name.

            The "characters" field must contain the exact names of all characters
            who appear in the selected scene.

            The "prompt" field must be a detailed image-generation prompt.

            The prompt MUST:
            - describe the selected scene in detail
            - describe the setting
            - describe the atmosphere
            - describe the lighting
            - describe important objects
            - describe what each character is doing
            - use the exact character names
            - reuse the established character descriptions from the previous step
            - maintain visual consistency with those character descriptions
            - be suitable as an image-generation prompt
            - describe ONE single illustration
            - NOT describe a comic
            - NOT describe multiple panels
            - NOT describe a collage
            - contain no text
            - contain no title
            - contain no labels
            - contain no borders

            If the book contains no suitable scene, return [].

            Do not return explanations.
            Do not use Markdown.
            Do not use ```json.
            Do not put any text before or after the JSON.

            Expected format:

            [
            {
                "name": "Chapter or scene name",
                "prompt": "Detailed image-generation prompt...",
                "characters": ["Character Name"]
            }
            ]
            """;

        Map<String, Object> requestBody = Map.of(
            "model", TEXT_MODEL,
            "previous_interaction_id", previousInteractionId,
            "input", inputPrompt,
            "response_format", Map.of(
                "type", "text",
                "mime_type", "application/json",
                "schema", schema
            )
        );

        String responseJson = executeGeminiRequest(requestBody);

        try {
            String jsonText = extractTextFromResponse(responseJson);

            if (jsonText == null || jsonText.isBlank()) {
                throw new RuntimeException(
                    "Gemini returned an empty chapter response"
                );
            }

            List<Map<String, Object>> chapters =
                objectMapper.readValue(
                    jsonText,
                    new TypeReference<List<Map<String, Object>>>() {}
                );

            // Enforce maximum 1 chapter even if Gemini returns more.
            List<Map<String, Object>> cappedChapters =
                chapters.stream()
                    .limit(1)
                    .collect(Collectors.toList());

            String newInteractionId =
                extractInteractionIdFromResponse(responseJson);

            return Map.of(
                "interactionId", newInteractionId,
                "chapters", cappedChapters
            );

        } catch (JsonProcessingException e) {

            log.error(
                "Failed to parse chapter response. Raw response: {}",
                responseJson,
                e
            );

            throw new RuntimeException(
                "Failed to parse chapters JSON from Gemini response: "
                + e.getMessage(),
                e
            );
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
    public String callGeminiPortrait(String characterName,String characterPrompt, String styleText, String previousInteractionId, Long projectId) {

        String combinedPrompt = """
            Create a character portrait for %s.

            Character description:
            %s

            Art style:
            %s

            Additional image requirements:
            %s

            The image must focus on this character.
            Create one complete illustration.
            Do not create a character sheet.
            Do not create multiple panels.
            Do not create a collage.
            Do not include text, labels, titles, captions, borders, or descriptions.
            """.formatted(
                characterName,
                characterPrompt,
                styleText,
                SYSTEM_INSTRUCTIONS
            );

        Map<String, Object> requestBody = Map.of(
            "model", IMAGE_MODEL,
            "previous_interaction_id", previousInteractionId,
            "input", combinedPrompt,
            "response_format", Map.of(
                "type", "image",
                "mime_type", "image/jpeg",
                "aspect_ratio", "1:1",
                "image_size", "1K"
            )
        );

        String responseJson = executeGeminiRequest(requestBody);

        String base64Image =
            extractBase64ImageFromResponse(responseJson);

        if (base64Image == null || base64Image.isBlank()) {
            throw new RuntimeException(
                "Gemini completed the portrait interaction but returned no image."
            );
        }

        return fileStorageService.savePortraitToLocalStorage(
            characterName,
            base64Image,
            projectId
        );
    }

    // NOTE: 1 illustration / chapter
    public String callGeminiIllustration(String chapterName, String chapterPrompt, String styleText, String previousInteractionId, List<String> base64ReferenceImages, Long projectId) {

        String inputPrompt = """
            Create the final chapter illustration for:

            %s

            Scene description:
            %s

            Art style:
            %s

            Additional requirements:
            %s

            Create exactly ONE complete illustration.

            The provided character images are visual references.
            Use them to maintain consistency in the appearance of the characters.

            Do not create a character sheet.
            Do not create a comic.
            Do not create multiple panels.
            Do not create a collage.
            Do not include text.
            Do not include titles.
            Do not include labels.
            Do not include captions.
            Do not include borders.
            """.formatted(
                chapterName,
                chapterPrompt,
                styleText,
                SYSTEM_INSTRUCTIONS
            );

        List<Object> inputParts = new ArrayList<>();

        inputParts.add(
            Map.of(
                "type", "text",
                "text", inputPrompt
            )
        );

        if (base64ReferenceImages != null) {
            for (String base64Img : base64ReferenceImages) {

                if (base64Img == null || base64Img.isBlank()) {
                    continue;
                }

                inputParts.add(
                    Map.of(
                        "type", "image",
                        "data", base64Img,
                        "mime_type", "image/jpeg"
                    )
                );
            }
        }

        Map<String, Object> requestBody = Map.of(
            "model", IMAGE_MODEL,
            "previous_interaction_id", previousInteractionId,
            "input", inputParts,
            "response_format", Map.of(
                "type", "image",
                "mime_type", "image/jpeg",
                "aspect_ratio", "16:9",
                "image_size", "1K"
            )
        );

        String responseJson = executeGeminiRequest(requestBody);

        String base64Image =
            extractBase64ImageFromResponse(responseJson);

        if (base64Image == null || base64Image.isBlank()) {
            throw new RuntimeException(
                "Gemini completed the illustration interaction but returned no image."
            );
        }

        return fileStorageService.saveIllustrationToLocalStorage(
            chapterName,
            base64Image,
            projectId
        );
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

            Map<String, Object> responseMap =
                objectMapper.readValue(
                    responseJson,
                    new TypeReference<Map<String, Object>>() {}
                );

            Object stepsObject = responseMap.get("steps");

            if (stepsObject instanceof List<?> steps) {

                for (int i = steps.size() - 1; i >= 0; i--) {

                    Object stepObject = steps.get(i);

                    if (!(stepObject instanceof Map<?, ?>)) {
                        continue;
                    }

                    Map<String, Object> step =
                        (Map<String, Object>) stepObject;

                    if (!"model_output".equals(step.get("type"))) {
                        continue;
                    }

                    Object contentObject = step.get("content");

                    if (!(contentObject instanceof List<?>)) {
                        continue;
                    }

                    List<?> contents =
                        (List<?>) contentObject;

                    for (int j = contents.size() - 1; j >= 0; j--) {

                        contentObject = contents.get(j);

                        if (!(contentObject instanceof Map<?, ?>)) {
                            continue;
                        }

                        Map<String, Object> content =
                            (Map<String, Object>) contentObject;

                        String type =
                            String.valueOf(content.get("type"));

                        if ("image".equals(type)) {

                            Object data = content.get("data");

                            if (data instanceof String base64
                                    && !base64.isBlank()) {

                                return base64;
                            }
                        }
                    }
                }
            }

            /*
            * Fallback in case the API response contains
            * the generated image in a top-level field.
            */
            Object imageBase64 =
                responseMap.get("image_base64");

            if (imageBase64 instanceof String base64
                    && !base64.isBlank()) {

                return base64;
            }

            log.error(
                "No image found in Gemini response: {}",
                responseJson
            );

            throw new RuntimeException(
                "Gemini response did not contain generated image data."
            );

        } catch (JsonProcessingException e) {

            throw new RuntimeException(
                "Failed to parse Gemini image response: "
                + e.getMessage(),
                e
            );
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
            Map<String, Object> responseMap =
                objectMapper.readValue(
                    responseJson,
                    new TypeReference<Map<String, Object>>() {}
                );

            Object id = responseMap.get("id");

            if (id instanceof String interactionId
                    && !interactionId.isBlank()) {

                return interactionId;
            }

            /*
            * Keep these fallbacks only if your actual API response
            * contains them.
            */
            Object interactionId =
                responseMap.get("interaction_id");

            if (interactionId instanceof String value
                    && !value.isBlank()) {

                return value;
            }

            Object camelCaseId =
                responseMap.get("interactionId");

            if (camelCaseId instanceof String value
                    && !value.isBlank()) {

                return value;
            }

            log.error(
                "Could not find interaction ID in response: {}",
                responseJson
            );

            throw new RuntimeException(
                "Missing interaction ID in Gemini API response"
            );

        } catch (JsonProcessingException e) {

            log.error(
                "Failed to parse interaction ID from: {}",
                responseJson,
                e
            );

            throw new RuntimeException(
                "Invalid JSON response from Gemini API",
                e
            );
        }
    }
}
