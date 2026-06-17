package com.example.ems.support.repository;

import com.example.ems.support.entity.MySupportAttachment;
import com.example.ems.support.entity.MySupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MySupportAttachmentRepository extends JpaRepository<MySupportAttachment, String> {
    List<MySupportAttachment> findByTicket(MySupportTicket ticket);
    List<MySupportAttachment> findByTicketId(Long ticketId);
}
