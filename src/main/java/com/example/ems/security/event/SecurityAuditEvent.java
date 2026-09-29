package com.example.ems.security.event;

import com.example.ems.audit.enums.AuditAction;
import com.example.ems.audit.enums.AuditStatus;

public record SecurityAuditEvent(
        String permission,
        AuditAction action,
        String resourceType,
        String resourceId,
        String failureReason,
        AuditStatus status
) {
    public static SecurityAuditEvent denied(String permission, String failureReason) {
        return new SecurityAuditEvent(
                permission,
                null,
                "SECURITY",
                permission,
                failureReason,
                AuditStatus.DENIED
        );
    }

    public static SecurityAuditEvent denied(String permission, AuditAction action, String resourceType, String resourceId, String failureReason) {
        return new SecurityAuditEvent(
                permission,
                action,
                resourceType,
                resourceId,
                failureReason,
                AuditStatus.DENIED
        );
    }
}
