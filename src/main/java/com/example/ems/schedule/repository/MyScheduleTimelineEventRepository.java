package com.example.ems.schedule.repository;

import com.example.ems.schedule.entity.MyScheduleTimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MyScheduleTimelineEventRepository extends JpaRepository<MyScheduleTimelineEvent, Long> {
    List<MyScheduleTimelineEvent> findByEmployeeEmailOrderByPerformedAtDesc(String email);
}
