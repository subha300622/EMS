package com.example.ems.support.repository;

import com.example.ems.support.entity.SupportTicketAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupportTicketAssignmentRepository extends JpaRepository<SupportTicketAssignment, Long> {
    List<SupportTicketAssignment> findByTicketIdOrderByAssignedAtDesc(Long ticketId);
}
