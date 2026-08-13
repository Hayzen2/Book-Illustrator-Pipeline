package com.example.BookIllustrator.dto.project.response;
import java.time.LocalDateTime;

import com.example.BookIllustrator.enums.GlobalStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProjectListResponse {
    private Long id;
    private String title;
    private GlobalStatus status;
    private LocalDateTime createdAt;
}
