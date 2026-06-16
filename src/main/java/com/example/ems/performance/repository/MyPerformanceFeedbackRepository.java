package com.example.ems.performance.repository;

import com.example.ems.performance.entity.MyPerformanceFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MyPerformanceFeedbackRepository extends JpaRepository<MyPerformanceFeedback, Long> {
    List<MyPerformanceFeedback> findByEmployeeEmail(String email);
    List<MyPerformanceFeedback> findByEmployeeEmailAndCycleId(String email, Long cycleId);
}
