package com.example.ems.schedule.repository;

import com.example.ems.schedule.entity.MyScheduleNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MyScheduleNotificationRepository extends JpaRepository<MyScheduleNotification, Long> {
    List<MyScheduleNotification> findByEmployeeEmailOrderByCreatedAtDesc(String email);
}
