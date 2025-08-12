package com.example.springsecuritytutorial.repository;

import com.example.springsecuritytutorial.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Integer> {
    User findByUserName(String username);
}