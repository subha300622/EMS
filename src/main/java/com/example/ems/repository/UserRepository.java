package com.example.ems.repository;

import com.example.ems.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByGmailAndPassword(String gmail, String password);
}