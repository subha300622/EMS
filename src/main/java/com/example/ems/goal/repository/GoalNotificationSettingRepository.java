package com.example.ems.goal.repository;

import com.example.ems.goal.domain.GoalNotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalNotificationSettingRepository extends JpaRepository<GoalNotificationSetting, Long> {

    List<GoalNotificationSetting> findByOrganizationId(Long organizationId);

    Optional<GoalNotificationSetting> findByOrganizationIdAndEventType(Long organizationId, String eventType);
}
