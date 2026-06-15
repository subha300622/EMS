package com.example.ems.auth.repository;

import com.example.ems.auth.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByWorkEmailAndPassword(String workEmail, String password);

    java.util.Optional<User> findByUserId(String userId);

    boolean existsByUserId(String userId);

    boolean existsByWorkEmail(String workEmail);

    boolean existsByEmployeeId(String employeeId);

    Optional<User> findByWorkEmail(String workEmail);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.workEmail) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.department) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.employeeId) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.userId) LIKE LOWER(CONCAT('%', :query, '%'))")
    java.util.List<User> searchUsers(@org.springframework.data.repository.query.Param("query") String query);

    java.util.List<User> findByRoleId(Long roleId);
}
