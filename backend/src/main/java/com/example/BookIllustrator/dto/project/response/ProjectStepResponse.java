package com.example.BookIllustrator.dto.project.response;

import java.time.LocalDateTime;

import com.example.BookIllustrator.enums.StepName;
import com.example.BookIllustrator.enums.StepStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectStepResponse {
    private StepName stepName;
    private StepStatus status;
    private String errorMessage;
    private LocalDateTime updatedAt;
}
