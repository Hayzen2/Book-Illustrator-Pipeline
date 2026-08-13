package com.example.BookIllustrator.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.BookIllustrator.entity.StoryCharacter;

@Repository
public interface StoryCharacterRepository extends JpaRepository<StoryCharacter, Long> {    
    Optional<List<StoryCharacter>> findByProjectId(Long projectId);
    Optional<StoryCharacter> findByProjectIdAndName(Long projectId, String name);
}
