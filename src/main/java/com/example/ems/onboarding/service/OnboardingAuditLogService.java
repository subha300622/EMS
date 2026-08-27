package com.example.ems.onboarding.service;

import com.example.ems.auth.entity.User;
import com.example.ems.onboarding.dto.audit.OnboardingAuditLogResponse;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.entity.OnboardingAuditLog;
import com.example.ems.onboarding.repository.OnboardingAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OnboardingAuditLogService {

    @Autowired
    private OnboardingAuditLogRepository auditLogRepository;

    @Autowired
    private OnboardingSecurityValidator securityValidator;

    @Transactional
    public void logAction(Onboarding onboarding, String action, String entityType, Long entityId, String details) {
        User user = null;
        try {
            user = securityValidator.getAuthenticatedUser();
        } catch (Exception e) {
            // Background or system event fallback
        }

        OnboardingAuditLog log = new OnboardingAuditLog();
        log.setOnboarding(onboarding);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setTimestamp(LocalDateTime.now());
        log.setDetails(details);

        if (user != null) {
            log.setPerformedBy(user.getEmployeeId() != null ? user.getEmployeeId() : "USER-" + user.getId());
            log.setPerformedByName(user.getWorkEmail());
        } else {
            log.setPerformedBy("SYSTEM");
            log.setPerformedByName("Automated System Event");
        }

        auditLogRepository.save(log);
    }

    public Page<OnboardingAuditLogResponse> getAuditLogs(Long onboardingId, int page, int size) {
        securityValidator.validateAndGetOnboarding(onboardingId);

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<OnboardingAuditLog> logs = auditLogRepository.findByOnboardingId(onboardingId, pageable);

        return logs.map(l -> {
            OnboardingAuditLogResponse dto = new OnboardingAuditLogResponse();
            dto.setAuditId(l.getId());
            dto.setOnboardingId(l.getOnboarding().getId());
            dto.setAction(l.getAction());
            dto.setEntityType(l.getEntityType());
            dto.setEntityId(l.getEntityId());
            dto.setPerformedBy(l.getPerformedBy());
            dto.setPerformedByName(l.getPerformedByName());
            dto.setTimestamp(l.getTimestamp());
            dto.setDetails(l.getDetails());
            return dto;
        });
    }
}
