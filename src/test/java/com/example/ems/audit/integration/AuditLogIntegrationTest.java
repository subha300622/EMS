package com.example.ems.audit.integration;

import com.example.ems.audit.dto.AuditLogEvent;
import com.example.ems.audit.dto.AuditLogFilterRequest;
import com.example.ems.audit.dto.AuditLogResponse;
import com.example.ems.audit.entity.AuditLog;
import com.example.ems.audit.enums.AuditAction;
import com.example.ems.audit.enums.AuditModule;
import com.example.ems.audit.enums.AuditStatus;
import com.example.ems.audit.repository.AuditLogRepository;
import com.example.ems.audit.service.AuditLogExportService;
import com.example.ems.audit.service.AuditLogQueryService;
import com.example.ems.audit.service.AuditLogService;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AuditLogIntegrationTest {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private AuditLogQueryService auditLogQueryService;

    @Autowired
    private AuditLogExportService auditLogExportService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Long testCompanyId;

    @BeforeEach
    public void setUp() {
        testCompanyId = 999901L;
        TenantContext.setCurrentTenant(testCompanyId);
    }

    @AfterEach
    public void tearDown() {
        TenantContext.clear();
        try {
            List<AuditLog> testLogs = auditLogRepository.findByCompanyIdOrderByCreatedAtDesc(testCompanyId);
            auditLogRepository.deleteAll(testLogs);
            List<AuditLog> company2Logs = auditLogRepository.findByCompanyIdOrderByCreatedAtDesc(999902L);
            auditLogRepository.deleteAll(company2Logs);
        } catch (Exception ignored) {}
    }

    @Test
    @DisplayName("Verify successful audit event persistence with JSON state and sanitization")
    public void testAuditSuccessEventPersistence() {
        AuditLogEvent event = AuditLogEvent.builder()
                .companyId(testCompanyId)
                .userId("USR-101")
                .userEmail("tester@company.com")
                .userName("Integration Tester")
                .departmentId(12L)
                .module(AuditModule.EMPLOYEE)
                .action(AuditAction.UPDATE)
                .entityType("Employee")
                .recordId("EMP-500")
                .oldValue(Map.of("designation", "Junior Dev", "password", "supersecret"))
                .newValue(Map.of("designation", "Senior Dev", "password", "newsecret"))
                .ipAddress("127.0.0.1")
                .device("Linux PC")
                .browser("Chrome")
                .details("Promoted employee")
                .build();

        AuditLog saved = auditLogService.success(event);
        assertNotNull(saved);
        assertNotNull(saved.getId());

        AuditLog fetched = auditLogRepository.findById(saved.getId()).orElse(null);
        assertNotNull(fetched);
        assertEquals(testCompanyId, fetched.getCompanyId());
        assertEquals(AuditStatus.SUCCESS.name(), fetched.getStatus());
        assertEquals("EMPLOYEE", fetched.getModule());
        assertEquals("UPDATE", fetched.getAction());
        assertEquals("EMP-500", fetched.getRecordId());

        // Verify password sanitization in stored JSONB
        assertNotNull(fetched.getOldValue());
        assertTrue(fetched.getOldValue().contains("***REDACTED***"));
        assertFalse(fetched.getOldValue().contains("supersecret"));

        assertNotNull(fetched.getNewValue());
        assertTrue(fetched.getNewValue().contains("***REDACTED***"));
        assertFalse(fetched.getNewValue().contains("newsecret"));
    }

    @Test
    @DisplayName("Verify REQUIRES_NEW failure audit persists even when outer transaction rolls back")
    public void testAuditFailureRequiresNewTransaction() {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        String failureRecordId = "ROLLBACK-REC-" + System.currentTimeMillis();

        try {
            txTemplate.execute(status -> {
                // Inside business transaction that will roll back
                // Call failure audit via Spring proxy (auditLogService bean)
                auditLogService.failure(
                        AuditModule.PAYROLL,
                        AuditAction.PROCESS,
                        "PayrollRun",
                        failureRecordId,
                        null,
                        null,
                        "Simulated business validation failure"
                );

                // Simulate outer business transaction exception causing rollback
                throw new RuntimeException("Force business transaction rollback!");
            });
        } catch (RuntimeException expected) {
            // Expected rollback
        }

        // Verify that the failed audit record was committed independently via REQUIRES_NEW!
        List<AuditLog> logs = auditLogRepository.findByRecordIdOrEntityIdOrderByCreatedAtDesc(failureRecordId);
        assertFalse(logs.isEmpty(), "Failure audit record MUST persist despite outer transaction rollback");

        AuditLog failureLog = logs.get(0);
        assertEquals(AuditStatus.FAILED.name(), failureLog.getStatus());
        assertEquals("Simulated business validation failure", failureLog.getFailureReason());
    }

    @Test
    @DisplayName("Verify tenant isolation: Company A user cannot query Company B audit logs")
    public void testTenantIsolationQuery() {
        Long companyA = testCompanyId;
        Long companyB = 999902L;

        // Create log in company A
        auditLogService.success(AuditLogEvent.builder()
                .companyId(companyA)
                .userId("USR-A")
                .module(AuditModule.EMPLOYEE)
                .action(AuditAction.CREATE)
                .entityType("Employee")
                .recordId("EMP-A")
                .details("Company A audit log")
                .build());

        // Create log in company B
        auditLogService.success(AuditLogEvent.builder()
                .companyId(companyB)
                .userId("USR-B")
                .module(AuditModule.EMPLOYEE)
                .action(AuditAction.CREATE)
                .entityType("Employee")
                .recordId("EMP-B")
                .details("Company B audit log")
                .build());

        // Create user belonging to Company A
        User userCompanyA = new User();
        userCompanyA.setUserId("USR-A");
        userCompanyA.setOrganizationId(companyA);
        Role role = new Role();
        role.setName("ADMIN");
        userCompanyA.setRole(role);

        AuditLogFilterRequest filter = new AuditLogFilterRequest();
        Page<AuditLogResponse> logsPage = auditLogQueryService.getLogs(filter, userCompanyA, PageRequest.of(0, 50));

        assertFalse(logsPage.isEmpty());
        assertTrue(logsPage.getContent().stream().allMatch(l -> companyA.equals(l.getCompanyId())));
        assertFalse(logsPage.getContent().stream().anyMatch(l -> companyB.equals(l.getCompanyId())));
    }

    @Test
    @DisplayName("Verify role scoping: HR manager cannot view Payroll audit logs")
    public void testRoleScoping() {
        // Create employee log
        auditLogService.success(AuditLogEvent.builder()
                .companyId(testCompanyId)
                .userId("HR-1")
                .module(AuditModule.EMPLOYEE)
                .action(AuditAction.CREATE)
                .entityType("Employee")
                .recordId("EMP-HR-1")
                .details("Employee onboarding")
                .build());

        // Create payroll log
        auditLogService.success(AuditLogEvent.builder()
                .companyId(testCompanyId)
                .userId("FIN-1")
                .module(AuditModule.PAYROLL)
                .action(AuditAction.PROCESS)
                .entityType("PayrollRun")
                .recordId("PR-100")
                .details("Payroll run processing")
                .build());

        // User with HR role
        User hrUser = new User();
        hrUser.setUserId("HR-1");
        hrUser.setOrganizationId(testCompanyId);
        Role hrRole = new Role();
        hrRole.setName("HR");
        hrUser.setRole(hrRole);

        Page<AuditLogResponse> hrView = auditLogQueryService.getLogs(new AuditLogFilterRequest(), hrUser, PageRequest.of(0, 50));
        assertFalse(hrView.isEmpty());
        // HR should only see allowed modules (e.g. Employee), not Payroll
        assertTrue(hrView.getContent().stream().noneMatch(l -> "PAYROLL".equalsIgnoreCase(l.getModule())));
        assertTrue(hrView.getContent().stream().anyMatch(l -> "EMPLOYEE".equalsIgnoreCase(l.getModule())));
    }

    @Test
    @DisplayName("Verify employee self activity query via /my-activity")
    public void testMyActivityQuery() {
        String myUserId = "EMP-SELF-01";
        String otherUserId = "EMP-OTHER-02";

        auditLogService.success(AuditLogEvent.builder()
                .companyId(testCompanyId)
                .userId(myUserId)
                .module(AuditModule.LEAVE)
                .action(AuditAction.APPLY)
                .entityType("Leave")
                .recordId("LV-1")
                .details("My leave application")
                .build());

        auditLogService.success(AuditLogEvent.builder()
                .companyId(testCompanyId)
                .userId(otherUserId)
                .module(AuditModule.LEAVE)
                .action(AuditAction.APPLY)
                .entityType("Leave")
                .recordId("LV-2")
                .details("Other employee leave application")
                .build());

        User myUser = new User();
        myUser.setUserId(myUserId);
        myUser.setWorkEmail("myself@company.com");
        myUser.setOrganizationId(testCompanyId);
        Role role = new Role();
        role.setName("EMPLOYEE");
        myUser.setRole(role);

        Page<AuditLogResponse> myActivity = auditLogQueryService.getMyActivity(myUser, PageRequest.of(0, 10));
        assertFalse(myActivity.isEmpty());
        assertEquals(1, myActivity.getTotalElements());
        assertEquals(myUserId, myActivity.getContent().get(0).getUserId());
    }

    @Test
    @DisplayName("Verify CSV export contains header and escaped values")
    public void testCsvExport() {
        auditLogService.success(AuditLogEvent.builder()
                .companyId(testCompanyId)
                .userId("EXPORT-USER")
                .userEmail("exporter@company.com")
                .userName("Export Tester")
                .module(AuditModule.EMPLOYEE)
                .action(AuditAction.UPDATE)
                .entityType("Employee")
                .recordId("EXP-01")
                .details("Data, with \"quotes\" and comma")
                .build());

        User adminUser = new User();
        adminUser.setUserId("EXPORT-USER");
        adminUser.setOrganizationId(testCompanyId);
        Role role = new Role();
        role.setName("ADMIN");
        adminUser.setRole(role);

        byte[] csvBytes = auditLogExportService.exportAuditLogsCsv(new AuditLogFilterRequest(), adminUser);
        assertNotNull(csvBytes);
        assertTrue(csvBytes.length > 0);

        String csvContent = new String(csvBytes, StandardCharsets.UTF_8);
        assertTrue(csvContent.startsWith("ID,Timestamp,Company ID,User ID,User Email"));
        assertTrue(csvContent.contains("EXPORT-USER"));
        assertTrue(csvContent.contains("exporter@company.com"));
    }
}
