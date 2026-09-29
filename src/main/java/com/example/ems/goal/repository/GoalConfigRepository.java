package com.example.ems.goal.repository;

import com.example.ems.goal.domain.GoalConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoalConfigRepository extends JpaRepository<GoalConfig, Long> {

    Optional<GoalConfig> findByOrganizationId(Long organizationId);
}
