package com.example.ems.training.repository;

import com.example.ems.training.entity.TrainingAttendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingAttendanceRepository extends JpaRepository<TrainingAttendance, Long> {
    List<TrainingAttendance> findByEnrollmentId(Long enrollmentId);
}
