package com.example.ems.superadmin.dashboard.service.query;

import com.example.ems.support.entity.SupportTicketStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SupportDashboardQuery {

    @PersistenceContext
    private EntityManager entityManager;

    private static final List<SupportTicketStatus> PENDING_TICKET_STATUSES = List.of(
            SupportTicketStatus.OPEN,
            SupportTicketStatus.ASSIGNED,
            SupportTicketStatus.IN_PROGRESS,
            SupportTicketStatus.WAITING_FOR_CUSTOMER,
            SupportTicketStatus.WAITING_FOR_DEVELOPMENT,
            SupportTicketStatus.REOPENED
    );

    public long queryPendingSupportTickets() {
        Long count = entityManager.createQuery(
                "SELECT COUNT(t) FROM MySupportTicket t " +
                "WHERE t.isDeleted = false AND t.status IN :statuses", Long.class)
                .setParameter("statuses", PENDING_TICKET_STATUSES)
                .getSingleResult();
        return count != null ? count : 0;
    }
}
