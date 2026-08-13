package com.example.BookIllustrator.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Table;
import lombok.Data;

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Project project; // Foreign key to Project

    @ManyToMany
    @JoinTable(
        name = "chapter_characters",
        joinColumns = @JoinColumn(name = "chapter_id"),
        inverseJoinColumns = @JoinColumn(name = "character_id")
    )
    private List<StoryCharacter> characters = new ArrayList<>();
}
