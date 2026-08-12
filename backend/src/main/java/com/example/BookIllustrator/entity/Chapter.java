package com.example.BookIllustrator.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;

@Entity
@Table(name = "chapters")
@Data
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "illustration_prompt", columnDefinition = "TEXT")
    private String illustrationPrompt;

    @Column(name = "illustration_image_path", length = 512)
    private String illustrationImagePath;

    // FetchType.LAZY is used to avoid loading the Project entity 
    // unless it's accessed
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Project project; // Foreign key to Project
}
