package com.example.ems.common.repository;

import com.example.ems.common.entity.DmsDocumentAuditLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DmsDocumentAuditLogRepository extends JpaRepository<DmsDocumentAuditLog, Long> {
    List<DmsDocumentAuditLog> findByDocumentIdOrderByPerformedAtDesc(Long documentId);
}
