package com.example.BookIllustrator.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import com.example.BookIllustrator.enums.GlobalStatus;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;
import jakarta.persistence.EnumType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.FetchType;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import jakarta.persistence.CascadeType;
import java.util.List;

@Entity
@Table(name = "projects")
@Data
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, name = "book_file_path", length = 512)
    private String bookFilePath;

    @Column(name = "book_interaction_id", length = 512)
    private String bookInteractionId;

    @Column(name = "art_style", columnDefinition = "TEXT")
    private String artStyle;

    @Column(nullable = false, name = "global_status")
    @Enumerated(EnumType.STRING)
    private GlobalStatus status = GlobalStatus.DRAFT;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user; // Foreign key to User

    // 2-way relationship with ProjectStep
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProjectStep> steps;
}
