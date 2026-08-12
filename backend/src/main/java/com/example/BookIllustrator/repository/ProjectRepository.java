package com.example.BookIllustrator.repository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.BookIllustrator.entity.Project;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<List<Project>> findByUserId(Long userId);
    Optional<Project> findByIdAndUserId(Long id, Long userId);
}