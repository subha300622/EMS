package com.example.ems.organization.repository;

import com.example.ems.organization.entity.OrganizationAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationAuditLogRepository extends JpaRepository<OrganizationAuditLog, Long>, JpaSpecificationExecutor<OrganizationAuditLog> {
}
