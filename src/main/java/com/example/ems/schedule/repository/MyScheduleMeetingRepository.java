package com.example.ems.schedule.repository;

import com.example.ems.schedule.entity.MyScheduleMeeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MyScheduleMeetingRepository extends JpaRepository<MyScheduleMeeting, Long> {

    List<MyScheduleMeeting> findByEmployeeEmail(String email);

    List<MyScheduleMeeting> findByEmployeeEmailAndStartDateTimeBetween(String email, LocalDateTime start, LocalDateTime end);

    List<MyScheduleMeeting> findByEmployeeEmailAndStartDateTimeAfter(String email, LocalDateTime dateTime);
}
