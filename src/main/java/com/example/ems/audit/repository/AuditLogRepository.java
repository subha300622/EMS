package com.example.ems.audit.repository;

import com.example.ems.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserEmailOrderByCreatedAtDesc(String email);

    List<AuditLog> findByUserIdOrderByCreatedAtDesc(String userId);

    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, String entityId);

    List<AuditLog> findAllByOrderByCreatedAtDesc();
}
