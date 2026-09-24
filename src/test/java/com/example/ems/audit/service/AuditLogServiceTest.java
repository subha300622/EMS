package com.example.ems.audit.service;

import com.example.ems.audit.dto.AuditLogEvent;
import com.example.ems.audit.entity.AuditLog;
import com.example.ems.audit.entity.Severity;
import com.example.ems.audit.enums.AuditAction;
import com.example.ems.audit.enums.AuditModule;
import com.example.ems.audit.enums.AuditStatus;
import com.example.ems.audit.repository.AuditLogRepository;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AuditDataSanitizer auditDataSanitizer;

    @Mock
    private AuditLogQueryService auditLogQueryService;

    @Mock
    private AuditLogExportService auditLogExportService;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        TenantContext.setCurrentTenant(100L);
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    public void tearDown() {
        TenantContext.clear();
    }

    @Test
    public void testRecordSuccessAuditEvent() {
        when(auditDataSanitizer.sanitizeToJson(any())).thenReturn("{\"field\":\"value\"}");

        AuditLogEvent event = AuditLogEvent.builder()
                .companyId(100L)
                .userId("EMP001")
                .userEmail("emp@example.com")
                .userName("John Doe")
                .departmentId(2L)
                .module(AuditModule.EMPLOYEE)
                .action(AuditAction.UPDATE)
                .entityType("Employee")
                .recordId("EMP001")
                .oldValue(Map.of("name", "John"))
                .newValue(Map.of("name", "John Doe"))
                .details("Updated profile name")
                .build();

        AuditLog saved = auditLogService.success(event);

        assertNotNull(saved);
        assertEquals(100L, saved.getCompanyId());
        assertEquals("EMP001", saved.getUserId());
        assertEquals("emp@example.com", saved.getUserEmail());
        assertEquals("EMPLOYEE", saved.getModule());
        assertEquals("UPDATE", saved.getAction());
        assertEquals(AuditStatus.SUCCESS.name(), saved.getStatus());
        assertEquals(Severity.INFO, saved.getSeverity());

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertEquals("EMP001", captor.getValue().getRecordId());
    }

    @Test
    public void testRecordFailureAuditEvent() {
        when(auditDataSanitizer.sanitizeToJson(any())).thenReturn(null);

        AuditLogEvent event = AuditLogEvent.builder()
                .companyId(100L)
                .userId("EMP002")
                .module(AuditModule.PAYROLL)
                .action(AuditAction.PROCESS)
                .entityType("PayrollRun")
                .recordId("PR-999")
                .failureReason("Salary calculation exception")
                .build();

        AuditLog saved = auditLogService.failure(event);

        assertNotNull(saved);
        assertEquals(AuditStatus.FAILED.name(), saved.getStatus());
        assertEquals("Salary calculation exception", saved.getFailureReason());
        assertEquals(Severity.WARNING, saved.getSeverity());

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    public void testLegacyLogActionCompatibility() {
        AuditLog result = auditLogService.logAction(
                "EMP003", "emp3@example.com", "CREATE", "Candidate", "CAND-12", "127.0.0.1", "Created candidate"
        );

        assertNotNull(result);
        assertEquals("EMP003", result.getUserId());
        assertEquals("emp3@example.com", result.getUserEmail());
        assertEquals("CREATE", result.getAction());
        assertEquals("Candidate", result.getEntityType());
        assertEquals("CAND-12", result.getEntityId());
        assertEquals("127.0.0.1", result.getIpAddress());
        verify(auditLogRepository).save(any(AuditLog.class));
    }
}
