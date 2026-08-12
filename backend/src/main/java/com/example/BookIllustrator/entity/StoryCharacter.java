package com.example.BookIllustrator.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "characters")
@Data
public class StoryCharacter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "portrait_image_path", length = 512)
    private String portraitImagePath;

    @Column(name = "image_prompt", columnDefinition = "TEXT")
    private String imagePrompt;

    // FetchType.LAZY is used to avoid loading the Project entity 
    // unless it's accessed
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Project project; // Foreign key to Project
}
