package com.example.ems.repository;

import com.example.ems.entity.Onboarding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OnboardingRepository extends JpaRepository<Onboarding, Long> {
    Optional<Onboarding> findByEmployeeId(Long employeeId);
    Optional<Onboarding> findByEmployeeEmail(String email);
    List<Onboarding> findByStatus(String status);
}
