package com.example.ems.goal.repository;

import com.example.ems.goal.domain.GoalPriorityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalPriorityRepository extends JpaRepository<GoalPriorityEntity, Long> {

    List<GoalPriorityEntity> findByOrganizationIdAndIsActiveTrueOrderBySortOrderAsc(Long organizationId);

    Optional<GoalPriorityEntity> findByOrganizationIdAndCode(Long organizationId, String code);
}
