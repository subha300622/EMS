package com.example.ems.training.repository;

import com.example.ems.training.entity.TrainingProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingProgressRepository extends JpaRepository<TrainingProgress, Long> {

    List<TrainingProgress> findByEmployeeId(Long employeeId);

    @Query("SELECT tp FROM TrainingProgress tp WHERE tp.employee.email = :email")
    List<TrainingProgress> findByEmployeeEmail(@Param("email") String email);

    @Query("SELECT tp FROM TrainingProgress tp WHERE tp.employee.manager.id = :managerId")
    List<TrainingProgress> findByEmployeeManagerId(@Param("managerId") Long managerId);

    List<TrainingProgress> findByAssignmentId(Long assignmentId);

    Optional<TrainingProgress> findByEmployeeIdAndAssignmentId(Long employeeId, Long assignmentId);

    Optional<TrainingProgress> findByAssignmentIdAndEmployeeId(Long assignmentId, Long employeeId);
}
