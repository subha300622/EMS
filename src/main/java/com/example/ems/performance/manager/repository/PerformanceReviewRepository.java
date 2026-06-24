package com.example.ems.performance.manager.repository;

import com.example.ems.performance.manager.entity.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("managerPerformanceReviewRepository")
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    Optional<PerformanceReview> findByEmployeeIdAndReviewCycle(Long employeeId, String reviewCycle);
    List<PerformanceReview> findByManagerIdAndReviewCycle(Long managerId, String reviewCycle);
}
