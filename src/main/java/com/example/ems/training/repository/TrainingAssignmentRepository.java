package com.example.ems.training.repository;

import com.example.ems.training.entity.TrainingAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingAssignmentRepository extends JpaRepository<TrainingAssignment, Long> {
    List<TrainingAssignment> findByDepartmentId(Long departmentId);
}
