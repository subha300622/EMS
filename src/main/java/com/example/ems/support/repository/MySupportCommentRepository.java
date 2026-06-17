package com.example.ems.support.repository;

import com.example.ems.support.entity.MySupportComment;
import com.example.ems.support.entity.MySupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MySupportCommentRepository extends JpaRepository<MySupportComment, Long> {
    List<MySupportComment> findByTicket(MySupportTicket ticket);
    List<MySupportComment> findByTicketId(Long ticketId);
}
