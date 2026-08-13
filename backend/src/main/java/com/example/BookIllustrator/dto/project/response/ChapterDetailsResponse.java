package com.example.BookIllustrator.dto.project.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChapterDetailsResponse {
    private Long id;
    private String name;
    private String illustrationPrompt;
    private String illustrationImagePath;
}
