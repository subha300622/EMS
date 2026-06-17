package com.example.ems.support.repository;

import com.example.ems.support.entity.MySupportTicket;
import com.example.ems.support.entity.MySupportTimelineActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MySupportTimelineActivityRepository extends JpaRepository<MySupportTimelineActivity, Long> {
    List<MySupportTimelineActivity> findByTicket(MySupportTicket ticket);
    List<MySupportTimelineActivity> findByTicketId(Long ticketId);
}
