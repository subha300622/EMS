package com.example.ems.training.repository;

import com.example.ems.training.entity.TrainingAttendance;
import com.example.ems.training.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingAttendanceRepository extends JpaRepository<TrainingAttendance, Long> {
    List<TrainingAttendance> findByTrainingId(Long trainingId);
    List<TrainingAttendance> findByTrainingIdAndSessionId(Long trainingId, Long sessionId);
    List<TrainingAttendance> findByEmployeeId(Long employeeId);
    Optional<TrainingAttendance> findByTrainingIdAndSessionIdAndEmployeeId(Long trainingId, Long sessionId, Long employeeId);
    Optional<TrainingAttendance> findByTrainingIdAndEmployeeId(Long trainingId, Long employeeId);
    long countByTrainingIdAndAttendanceStatus(Long trainingId, AttendanceStatus attendanceStatus);
}
