package com.example.BookIllustrator.dto.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.example.BookIllustrator.enums.StepName;
import com.example.BookIllustrator.enums.StepStatus;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectStepResponse {
    private Long id;
    private StepName stepName;
    private StepStatus status;
    private String errorMessage;
}
