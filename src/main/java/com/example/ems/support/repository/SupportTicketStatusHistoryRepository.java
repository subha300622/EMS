package com.example.ems.support.repository;

import com.example.ems.support.entity.SupportTicketStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketStatusHistoryRepository extends JpaRepository<SupportTicketStatusHistory, Long> {
    List<SupportTicketStatusHistory> findByTicketIdOrderByChangedAtAsc(Long ticketId);
}
