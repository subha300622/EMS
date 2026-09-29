package com.example.ems.support.repository;

import com.example.ems.support.entity.SupportEscalationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportEscalationHistoryRepository extends JpaRepository<SupportEscalationHistory, Long> {
    List<SupportEscalationHistory> findByTicketIdOrderByTriggeredAtAsc(Long ticketId);
    boolean existsByTicketIdAndLevel(Long ticketId, Integer level);
    Optional<SupportEscalationHistory> findByTicketIdAndLevel(Long ticketId, Integer level);
}
