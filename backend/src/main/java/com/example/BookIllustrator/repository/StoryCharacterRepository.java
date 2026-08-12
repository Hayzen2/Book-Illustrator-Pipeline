package com.example.BookIllustrator.repository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.BookIllustrator.entity.StoryCharacter;

@Repository
public interface StoryCharacterRepository extends JpaRepository<StoryCharacter, Long> {    
}
