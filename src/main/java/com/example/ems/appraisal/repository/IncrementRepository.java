package com.example.ems.appraisal.repository;

import com.example.ems.appraisal.entity.Increment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface IncrementRepository extends JpaRepository<Increment, Long> {
    List<Increment> findByEmployeeId(Long employeeId);
    List<Increment> findByStatus(String status);
    Page<Increment> findByStatus(String status, Pageable pageable);
    List<Increment> findByEmployeeEmail(String email);
}
