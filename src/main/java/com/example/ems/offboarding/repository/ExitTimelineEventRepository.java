package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.ExitTimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExitTimelineEventRepository extends JpaRepository<ExitTimelineEvent, Long> {
    List<ExitTimelineEvent> findByOffboardingIdOrderByEventDateAsc(Long offboardingId);
}
