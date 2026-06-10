package com.example.ems.repository;

import com.example.ems.entity.TrainingEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingEnrollmentRepository extends JpaRepository<TrainingEnrollment, Long> {
    List<TrainingEnrollment> findByEmployeeId(Long employeeId);
    List<TrainingEnrollment> findBySessionId(Long sessionId);
    Optional<TrainingEnrollment> findByEmployeeIdAndSessionId(Long employeeId, Long sessionId);
    List<TrainingEnrollment> findByStatus(String status);
}
