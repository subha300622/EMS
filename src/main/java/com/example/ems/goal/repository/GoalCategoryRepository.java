package com.example.ems.goal.repository;

import com.example.ems.goal.domain.GoalCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalCategoryRepository extends JpaRepository<GoalCategory, Long> {

    List<GoalCategory> findByOrganizationIdAndIsActiveTrue(Long organizationId);

    Optional<GoalCategory> findByOrganizationIdAndCode(Long organizationId, String code);
}
