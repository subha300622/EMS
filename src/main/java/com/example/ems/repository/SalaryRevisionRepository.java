package com.example.ems.repository;

import com.example.ems.entity.SalaryRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryRevisionRepository extends JpaRepository<SalaryRevision, Long> {
    List<SalaryRevision> findByEmployeeIdOrderByEffectiveDateDesc(Long employeeId);
}
