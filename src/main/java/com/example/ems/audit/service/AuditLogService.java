package com.example.ems.audit.service;

import com.example.ems.audit.entity.AuditLog;
import com.example.ems.audit.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public AuditLog logAction(String userId, String userEmail, String action, String entityType, String entityId, String ipAddress, String details) {
        AuditLog log = new AuditLog(userId, userEmail, action, entityType, entityId, ipAddress, details);
        return auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Optional<AuditLog> getLogById(Long id) {
        return auditLogRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByUser(String userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByEntity(String entityType, String entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    @Transactional(readOnly = true)
    public byte[] exportLogsToCsv() {
        List<AuditLog> logs = getAllLogs();
        StringBuilder csv = new StringBuilder("ID,Timestamp,User ID,User Email,Action,Entity Type,Entity ID,IP Address,Details\n");
        for (AuditLog log : logs) {
            csv.append(log.getId()).append(",")
               .append(log.getCreatedAt()).append(",")
               .append(escapeCsvField(log.getUserId())).append(",")
               .append(escapeCsvField(log.getUserEmail())).append(",")
               .append(escapeCsvField(log.getAction())).append(",")
               .append(escapeCsvField(log.getEntityType())).append(",")
               .append(escapeCsvField(log.getEntityId())).append(",")
               .append(escapeCsvField(log.getIpAddress())).append(",")
               .append(escapeCsvField(log.getDetails())).append("\n");
        }
        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private String escapeCsvField(String field) {
        if (field == null) return "";
        String clean = field.replace("\"", "\"\"");
        if (clean.contains(",") || clean.contains("\n") || clean.contains("\"")) {
            return "\"" + clean + "\"";
        }
        return clean;
    }
}
