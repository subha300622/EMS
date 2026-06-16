package com.example.ems.performance.repository;

import com.example.ems.performance.entity.MyPerformanceTimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MyPerformanceTimelineEventRepository extends JpaRepository<MyPerformanceTimelineEvent, Long> {
    List<MyPerformanceTimelineEvent> findByEmployeeEmailOrderByDateDesc(String email);
}
