package com.example.ems.settings.repository;

import com.example.ems.settings.entity.UserBackupCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBackupCodeRepository extends JpaRepository<UserBackupCode, Long> {
    List<UserBackupCode> findByUserEmail(String userEmail);
    void deleteByUserEmail(String userEmail);
}
