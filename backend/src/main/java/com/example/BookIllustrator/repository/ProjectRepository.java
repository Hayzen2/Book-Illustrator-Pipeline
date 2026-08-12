package com.example.BookIllustrator.repository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.BookIllustrator.entity.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
}