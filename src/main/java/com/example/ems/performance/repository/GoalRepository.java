package com.example.ems.performance.repository;

import com.example.ems.performance.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    
    List<Goal> findByEmployeeId(Long employeeId);
    
    List<Goal> findByEmployeeEmail(String email);
    
    List<Goal> findByManagerId(Long managerId);
    
    Optional<Goal> findByIdAndEmployeeEmail(Long id, String email);
    
    long countByEmployeeIdAndStatus(Long employeeId, String status);
    
    long countByEmployeeId(Long employeeId);
}
