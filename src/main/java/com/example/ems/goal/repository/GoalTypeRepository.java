package com.example.ems.goal.repository;

import com.example.ems.goal.domain.GoalTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalTypeRepository extends JpaRepository<GoalTypeEntity, Long> {

    List<GoalTypeEntity> findByOrganizationIdAndIsActiveTrue(Long organizationId);

    Optional<GoalTypeEntity> findByOrganizationIdAndCode(Long organizationId, String code);
}
