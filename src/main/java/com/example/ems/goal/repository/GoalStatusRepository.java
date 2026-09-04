package com.example.ems.goal.repository;

import com.example.ems.goal.domain.GoalStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalStatusRepository extends JpaRepository<GoalStatusEntity, Long> {

    List<GoalStatusEntity> findByOrganizationIdAndIsActiveTrue(Long organizationId);

    Optional<GoalStatusEntity> findByOrganizationIdAndCode(Long organizationId, String code);
}
