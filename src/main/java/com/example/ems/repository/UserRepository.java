package com.example.ems.repository;

import com.example.ems.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByWorkEmailAndPassword(String workEmail, String password);

    boolean existsByWorkEmail(String workEmail);

    boolean existsByEmployeeId(String employeeId);

    Optional<User> findByWorkEmail(String workEmail);
}