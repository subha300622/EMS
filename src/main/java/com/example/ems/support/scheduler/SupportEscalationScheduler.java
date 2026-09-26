package com.example.ems.support.scheduler;

import com.example.ems.audit.enums.AuditAction;
import com.example.ems.audit.enums.AuditModule;
import com.example.ems.audit.service.AuditLogService;
import com.example.ems.support.dto.SupportEscalationRulesDto;
import com.example.ems.support.entity.*;
import com.example.ems.support.repository.MySupportTicketRepository;
import com.example.ems.support.repository.SupportEscalationHistoryRepository;
import com.example.ems.support.service.SupportSlaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class SupportEscalationScheduler {

    private static final Logger log = LoggerFactory.getLogger(SupportEscalationScheduler.class);

    @Autowired
    private MySupportTicketRepository ticketRepository;

    @Autowired
    private SupportEscalationHistoryRepository escalationHistoryRepository;

    @Autowired
    private SupportSlaService slaService;

    @Autowired(required = false)
    private AuditLogService auditLogService;

    private static final List<SupportTicketStatus> TERMINAL_STATUSES = List.of(
            SupportTicketStatus.CLOSED,
            SupportTicketStatus.RESOLVED,
            SupportTicketStatus.REJECTED,
            SupportTicketStatus.DUPLICATE
    );

    @Scheduled(fixedDelay = 60000, initialDelay = 10000)
    @Transactional
    public void evaluateOverdueTicketsAndEscalations() {
        LocalDateTime now = LocalDateTime.now();
        List<MySupportTicket> overdueCandidates = ticketRepository.findOverdueCandidateTickets(TERMINAL_STATUSES, now);

        if (overdueCandidates == null || overdueCandidates.isEmpty()) {
            return;
        }

        for (MySupportTicket ticket : overdueCandidates) {
            try {
                processTicketEscalation(ticket, now);
            } catch (Exception e) {
                log.error("Error evaluating escalation for ticket #{}", ticket.getTicketNumber(), e);
            }
        }
    }

    private void processTicketEscalation(MySupportTicket ticket, LocalDateTime now) {
        boolean updated = false;

        if (!ticket.isOverdue()) {
            ticket.setOverdue(true);
            updated = true;
            log.info("Marked ticket #{} as overdue", ticket.getTicketNumber());
        }

        if (ticket.getOrganization() == null) {
            if (updated) ticketRepository.save(ticket);
            return;
        }

        long overdueMinutes = Duration.between(ticket.getDueDate(), now).toMinutes();
        if (overdueMinutes < 0) {
            if (updated) ticketRepository.save(ticket);
            return;
        }

        SupportEscalationRulesDto rulesDto = slaService.getEscalationRules(ticket.getOrganization().getId());
        if (rulesDto == null || rulesDto.getRules() == null) {
            if (updated) ticketRepository.save(ticket);
            return;
        }

        for (SupportEscalationRulesDto.EscalationRuleItem rule : rulesDto.getRules()) {
            if (overdueMinutes >= rule.getTriggerAfterMinutes()) {
                // Idempotency check: unique(ticket_id, escalation_level)
                boolean alreadyEscalated = escalationHistoryRepository.existsByTicketIdAndLevel(ticket.getId(), rule.getLevel());
                if (!alreadyEscalated) {
                    SupportEscalationHistory history = new SupportEscalationHistory(
                            ticket,
                            rule.getLevel(),
                            "SLA breached (overdue by " + overdueMinutes + " minutes)",
                            rule.getAction(),
                            "COMPLETED"
                    );
                    escalationHistoryRepository.save(history);

                    ticket.setEscalated(true);
                    ticket.setEscalationLevel(Math.max(ticket.getEscalationLevel(), rule.getLevel()));
                    updated = true;

                    if (auditLogService != null) {
                        auditLogService.success(AuditModule.SUPPORT, AuditAction.ESCALATE, "MySupportTicket",
                                String.valueOf(ticket.getId()), String.valueOf(rule.getLevel() - 1),
                                String.valueOf(rule.getLevel()),
                                "Automatic SLA escalation to level " + rule.getLevel() + " (action: " + rule.getAction() + ")");
                    }

                    log.info("Triggered escalation level {} with action {} for ticket #{}",
                            rule.getLevel(), rule.getAction(), ticket.getTicketNumber());
                }
            }
        }

        if (updated) {
            ticket.setUpdatedAt(now);
            ticketRepository.save(ticket);
        }
    }
}
