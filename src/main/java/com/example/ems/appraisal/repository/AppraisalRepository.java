package com.example.ems.appraisal.repository;

import com.example.ems.appraisal.entity.Appraisal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppraisalRepository extends JpaRepository<Appraisal, Long> {
    List<Appraisal> findByEmployeeId(Long employeeId);
    List<Appraisal> findByCycleId(Long cycleId);
    List<Appraisal> findByStatus(String status);
    Optional<Appraisal> findByEmployeeEmailAndCycleId(String email, Long cycleId);
    List<Appraisal> findByEmployeeEmail(String email);
}
