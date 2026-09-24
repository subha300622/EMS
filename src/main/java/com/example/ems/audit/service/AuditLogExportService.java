package com.example.ems.audit.service;

import com.example.ems.audit.dto.AuditLogFilterRequest;
import com.example.ems.auth.entity.User;

import java.util.Collection;

public interface AuditLogExportService {

    byte[] exportAuditLogsCsv(AuditLogFilterRequest request, User currentUser);

    byte[] exportLogsToCsv(Collection<String> allowedModules);
}
