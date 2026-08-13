package com.example.BookIllustrator.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.BookIllustrator.entity.Chapter;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    Optional<List<Chapter>> findByProjectId(Long projectId);
}
