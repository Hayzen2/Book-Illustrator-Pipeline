package com.example.BookIllustrator.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.BookIllustrator.enums.StepName;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.Column;
import com.example.BookIllustrator.enums.StepStatus;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import org.hibernate.annotations.OnDeleteAction;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "project_steps", 
    uniqueConstraints = {
        // For each project, each step name should be unique
        @UniqueConstraint(
            name = "unique_project_step",
            columnNames = {
                "project_id", "step_name"
            }
        )
    })
@Data
public class ProjectStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "step_name")
    private StepName stepName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StepStatus status = StepStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    @Column(name = "interaction_id", length = 512)
    private String interactionId;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Project project; // Foreign key to Project
}
