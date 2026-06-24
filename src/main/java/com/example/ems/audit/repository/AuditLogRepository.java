package com.example.ems.audit.repository;

import com.example.ems.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    List<AuditLog> findByUserEmailOrderByCreatedAtDesc(String email);

    List<AuditLog> findByUserIdOrderByCreatedAtDesc(String userId);

    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, String entityId);

    List<AuditLog> findAllByOrderByCreatedAtDesc();

    long countByFlaggedTrue();

    long countByFlaggedTrueAndEntityTypeIn(Collection<String> entityTypes);

    long countByEntityTypeInAndCreatedAtAfter(Collection<String> entityTypes, LocalDateTime datetime);

    long countByCreatedAtAfter(LocalDateTime datetime);
}
