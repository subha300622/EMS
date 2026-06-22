package com.example.ems.performance.repository;

import com.example.ems.performance.entity.MyGoal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MyGoalRepository extends JpaRepository<MyGoal, Long> {
    
    @Query("SELECT g FROM MyGoal g WHERE g.employee.email = :email " +
           "AND (:cycleId IS NULL OR g.cycle.id = :cycleId) " +
           "AND (:status IS NULL OR g.status = :status) " +
           "AND (:category IS NULL OR g.category = :category)")
    Page<MyGoal> findByFilters(@Param("email") String email,
                                @Param("cycleId") Long cycleId,
                                @Param("status") String status,
                                @Param("category") String category,
                                Pageable pageable);

    List<MyGoal> findByEmployeeEmailAndCycleId(String email, Long cycleId);
    
    Optional<MyGoal> findByIdAndEmployeeEmail(Long id, String email);

    List<MyGoal> findByEmployeeId(Long employeeId);
}
