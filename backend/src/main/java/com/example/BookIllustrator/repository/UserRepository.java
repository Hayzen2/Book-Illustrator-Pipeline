package com.example.BookIllustrator.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.BookIllustrator.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}