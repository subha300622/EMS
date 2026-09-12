package com.example.ems.leave.repository;

import com.example.ems.leave.entity.LeaveRequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestHistoryRepository extends JpaRepository<LeaveRequestHistory, Long> {
    List<LeaveRequestHistory> findByLeaveIdOrderByPerformedAtAsc(Long leaveId);
}
