package com.example.ems.repository;

import com.example.ems.entity.PerformanceCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PerformanceCycleRepository extends JpaRepository<PerformanceCycle, Long> {
    List<PerformanceCycle> findByStatus(String status);
}
