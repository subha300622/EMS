package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.OffboardingTask;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OffboardingTaskRepository extends JpaRepository<OffboardingTask, Long> {
    List<OffboardingTask> findByOffboardingId(Long offboardingId);
}
