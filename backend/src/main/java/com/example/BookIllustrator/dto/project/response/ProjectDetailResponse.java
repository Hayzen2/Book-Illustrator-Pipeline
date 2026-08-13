package com.example.BookIllustrator.dto.project.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
import com.example.BookIllustrator.enums.GlobalStatus;
import com.example.BookIllustrator.dto.project.response.ProjectStepResponse;
import com.example.BookIllustrator.dto.project.response.CharacterDetailsResponse;

@Data
@AllArgsConstructor
public class ProjectDetailResponse {
    private Long id;
    private String title;
    private String artStyle;
    private GlobalStatus globalStatus;
    private List<ProjectStepResponse> steps;
    private List<CharacterDetailsResponse> characters;
    private List<ChapterDetailsResponse> chapters;
}
