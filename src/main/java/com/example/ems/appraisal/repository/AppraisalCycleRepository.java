package com.example.ems.appraisal.repository;

import com.example.ems.appraisal.entity.AppraisalCycle;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppraisalCycleRepository extends JpaRepository<AppraisalCycle, Long> {
    List<AppraisalCycle> findByStatus(String status);
}
