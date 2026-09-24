package com.example.ems.audit.listener;

import com.example.ems.audit.service.AuditLogService;
import com.example.ems.security.event.SecurityAuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SecurityAuditEventListener {

    private static final Logger log = LoggerFactory.getLogger(SecurityAuditEventListener.class);

    @Autowired
    private AuditLogService auditLogService;

    @EventListener
    public void onSecurityAuditEvent(SecurityAuditEvent event) {
        if (event == null) {
            return;
        }
        try {
            auditLogService.recordSecurityAudit(event);
        } catch (Exception e) {
            log.error("Failed to record SecurityAuditEvent: {}", e.getMessage(), e);
        }
    }
}
