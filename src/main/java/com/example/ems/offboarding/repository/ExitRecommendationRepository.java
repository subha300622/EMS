package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.ExitRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ExitRecommendationRepository extends JpaRepository<ExitRecommendation, Long> {
    Optional<ExitRecommendation> findByEmployeeId(Long employeeId);
}
