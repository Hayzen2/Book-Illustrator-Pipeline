package com.example.BookIllustrator.dto.project.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CharacterDetailsResponse {
    private Long id;
    private String name;
    private String imagePrompt;
    private String portraitImagePath;
}
