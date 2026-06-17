package com.example.ems.settings.repository;

import com.example.ems.settings.entity.EmployeeSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeSettingRepository extends JpaRepository<EmployeeSetting, Long> {
    Optional<EmployeeSetting> findByUserEmail(String userEmail);
}
