package com.example.ems.goal.repository;

import com.example.ems.goal.domain.GoalVisibilitySetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalVisibilitySettingRepository extends JpaRepository<GoalVisibilitySetting, Long> {

    List<GoalVisibilitySetting> findByOrganizationIdAndIsActiveTrue(Long organizationId);

    Optional<GoalVisibilitySetting> findByOrganizationIdAndVisibilityCode(Long organizationId, String visibilityCode);
}
