package com.example.ems.performance.repository;

import com.example.ems.performance.entity.PerformanceReview;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    List<PerformanceReview> findByEmployeeId(Long employeeId);
    List<PerformanceReview> findByCycleId(Long cycleId);
    List<PerformanceReview> findByReviewType(String reviewType);
    List<PerformanceReview> findByStatus(String status);
    List<PerformanceReview> findByReviewerId(Long reviewerId);
}
