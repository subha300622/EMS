package com.example.ems.repository;

import com.example.ems.entity.Offboarding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OffboardingRepository extends JpaRepository<Offboarding, Long> {
    Optional<Offboarding> findByEmployeeId(Long employeeId);
    Optional<Offboarding> findByEmployeeEmail(String email);
    List<Offboarding> findByStatus(String status);
}
