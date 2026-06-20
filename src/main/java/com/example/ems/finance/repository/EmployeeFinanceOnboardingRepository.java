package com.example.ems.finance.repository;

import com.example.ems.finance.entity.EmployeeFinanceOnboarding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeFinanceOnboardingRepository extends JpaRepository<EmployeeFinanceOnboarding, Long> {
    Optional<EmployeeFinanceOnboarding> findByEmployeeId(Long employeeId);
    List<EmployeeFinanceOnboarding> findByStatus(String status);
    long countByStatus(String status);
}
