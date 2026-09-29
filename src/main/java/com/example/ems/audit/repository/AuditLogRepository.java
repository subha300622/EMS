package com.example.ems.audit.repository;

import com.example.ems.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    List<AuditLog> findByUserEmailOrderByCreatedAtDesc(String email);

    List<AuditLog> findByUserIdOrderByCreatedAtDesc(String userId);

    List<AuditLog> findByCompanyIdAndUserIdOrderByCreatedAtDesc(Long companyId, String userId);

    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Page<AuditLog> findByCompanyIdAndUserIdOrderByCreatedAtDesc(Long companyId, String userId, Pageable pageable);

    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, String entityId);

    List<AuditLog> findByEntityTypeAndRecordIdOrderByCreatedAtDesc(String entityType, String recordId);

    @Query("SELECT a FROM AuditLog a WHERE (a.recordId = :recordId OR a.entityId = :recordId) ORDER BY a.createdAt DESC")
    List<AuditLog> findByRecordIdOrEntityIdOrderByCreatedAtDesc(@Param("recordId") String recordId);

    @Query("SELECT a FROM AuditLog a WHERE a.companyId = :companyId AND (a.recordId = :recordId OR a.entityId = :recordId) ORDER BY a.createdAt DESC")
    List<AuditLog> findByCompanyIdAndRecordIdOrEntityIdOrderByCreatedAtDesc(@Param("companyId") Long companyId, @Param("recordId") String recordId);

    List<AuditLog> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    List<AuditLog> findAllByOrderByCreatedAtDesc();

    long countByFlaggedTrue();

    long countByFlaggedTrueAndEntityTypeIn(Collection<String> entityTypes);

    long countByEntityTypeInAndCreatedAtAfter(Collection<String> entityTypes, LocalDateTime datetime);

    long countByCreatedAtAfter(LocalDateTime datetime);

    long countByUserEmailInAndCreatedAtAfter(Collection<String> userEmails, LocalDateTime datetime);
}
