package com.example.BookIllustrator.dto.project;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjectCreateRequest {
    @NotBlank(message = "Project title is required")
    private String title;

    @NotBlank(message = "Book content/text is required")
    private String bookText;
}
