package com.example.ems.repository;

import com.example.ems.entity.Increment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncrementRepository extends JpaRepository<Increment, Long> {
    List<Increment> findByEmployeeId(Long employeeId);
    List<Increment> findByStatus(String status);
}
