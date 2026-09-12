package com.example.ems.goal.repository;

import com.example.ems.goal.domain.GoalNumberFormat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoalNumberFormatRepository extends JpaRepository<GoalNumberFormat, Long> {

    Optional<GoalNumberFormat> findByOrganizationId(Long organizationId);
}
