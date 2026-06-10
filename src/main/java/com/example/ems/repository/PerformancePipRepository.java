package com.example.ems.repository;

import com.example.ems.entity.PerformancePip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PerformancePipRepository extends JpaRepository<PerformancePip, Long> {
    List<PerformancePip> findByEmployeeId(Long employeeId);
    List<PerformancePip> findByStatus(String status);
}
