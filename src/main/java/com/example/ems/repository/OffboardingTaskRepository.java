package com.example.ems.repository;

import com.example.ems.entity.OffboardingTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OffboardingTaskRepository extends JpaRepository<OffboardingTask, Long> {
    List<OffboardingTask> findByOffboardingId(Long offboardingId);
}
