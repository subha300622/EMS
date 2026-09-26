package com.example.ems.employee.listener;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.event.*;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.service.EmployeeLifecycleAuditService;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.OrganizationAuditLog;
import com.example.ems.organization.repository.OrganizationAuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeLifecycleAuditListenerTest {

    @Mock
    private OrganizationAuditLogRepository auditLogRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private ObjectMapper objectMapper;
    private EmployeeLifecycleAuditService auditService;
    private EmployeeLifecycleAuditListener listener;

    private final Long orgId = 10L;
    private final Long employeeId = 555L;
    private final Instant now = Instant.parse("2026-09-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        auditService = new EmployeeLifecycleAuditService(
                auditLogRepository,
                employeeRepository,
                objectMapper
        );
        listener = new EmployeeLifecycleAuditListener(auditService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── Case 1: EmployeeJoinedEvent ──────────────────────────────────────────
    @Test
    @DisplayName("Case 1: handleEmployeeJoined records EMPLOYEE_JOINED compliance audit log")
    void testHandleEmployeeJoined_AuditLogged() {
        EmployeeJoinedEvent event = new EmployeeJoinedEvent(employeeId, orgId, now);

        listener.handleEmployeeJoined(event);

        ArgumentCaptor<OrganizationAuditLog> captor = ArgumentCaptor.forClass(OrganizationAuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        OrganizationAuditLog log = captor.getValue();
        assertEquals(orgId, log.getOrganizationId());
        assertEquals("EMPLOYEE_JOINED", log.getAction());
        assertEquals("Employee", log.getEntity());
        assertEquals(employeeId, log.getEntityId());
        assertEquals("SYSTEM", log.getPerformedBy());
        assertEquals(now, log.getPerformedAt());
        assertNull(log.getOldValues());
        assertTrue(log.getNewValues().contains("\"status\":\"JOINED\""));
        assertTrue(log.getNewValues().contains("\"employeeId\":555"));
    }

    // ── Case 2: EmployeeActivatedEvent ───────────────────────────────────────
    @Test
    @DisplayName("Case 2: handleEmployeeActivated records EMPLOYEE_ACTIVATED compliance audit log")
    void testHandleEmployeeActivated_AuditLogged() {
        EmployeeActivatedEvent event = new EmployeeActivatedEvent(employeeId, orgId, now);

        listener.handleEmployeeActivated(event);

        ArgumentCaptor<OrganizationAuditLog> captor = ArgumentCaptor.forClass(OrganizationAuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        OrganizationAuditLog log = captor.getValue();
        assertEquals(orgId, log.getOrganizationId());
        assertEquals("EMPLOYEE_ACTIVATED", log.getAction());
        assertEquals(employeeId, log.getEntityId());
        assertNull(log.getOldValues());
        assertTrue(log.getNewValues().contains("\"status\":\"ACTIVE\""));
    }

    // ── Case 3: EmployeeTransferredEvent ─────────────────────────────────────
    @Test
    @DisplayName("Case 3: handleEmployeeTransferred records EMPLOYEE_TRANSFERRED with department changes")
    void testHandleEmployeeTransferred_AuditLogged() {
        Long fromDeptId = 1L;
        Long toDeptId = 2L;
        EmployeeTransferredEvent event = new EmployeeTransferredEvent(employeeId, orgId, fromDeptId, toDeptId, now);

        listener.handleEmployeeTransferred(event);

        ArgumentCaptor<OrganizationAuditLog> captor = ArgumentCaptor.forClass(OrganizationAuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        OrganizationAuditLog log = captor.getValue();
        assertEquals(orgId, log.getOrganizationId());
        assertEquals("EMPLOYEE_TRANSFERRED", log.getAction());
        assertEquals(employeeId, log.getEntityId());
        assertTrue(log.getOldValues().contains("\"departmentId\":1"));
        assertTrue(log.getNewValues().contains("\"departmentId\":2"));
    }

    // ── Case 4: EmployeeManagerChangedEvent (New Manager) ────────────────────
    @Test
    @DisplayName("Case 4: handleEmployeeManagerChanged records EMPLOYEE_MANAGER_CHANGED with manager changes")
    void testHandleEmployeeManagerChanged_WithNewManager_AuditLogged() {
        Long oldManagerId = 101L;
        Long newManagerId = 202L;
        EmployeeManagerChangedEvent event = new EmployeeManagerChangedEvent(employeeId, orgId, oldManagerId, newManagerId, now);

        listener.handleEmployeeManagerChanged(event);

        ArgumentCaptor<OrganizationAuditLog> captor = ArgumentCaptor.forClass(OrganizationAuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        OrganizationAuditLog log = captor.getValue();
        assertEquals(orgId, log.getOrganizationId());
        assertEquals("EMPLOYEE_MANAGER_CHANGED", log.getAction());
        assertEquals(employeeId, log.getEntityId());
        assertTrue(log.getOldValues().contains("\"managerId\":101"));
        assertTrue(log.getNewValues().contains("\"managerId\":202"));
    }

    // ── Case 5: EmployeeManagerChangedEvent (Manager Removed) ─────────────────
    @Test
    @DisplayName("Case 5: handleEmployeeManagerChanged records manager removal (null new manager)")
    void testHandleEmployeeManagerChanged_ManagerRemoved_AuditLogged() {
        Long oldManagerId = 101L;
        EmployeeManagerChangedEvent event = new EmployeeManagerChangedEvent(employeeId, orgId, oldManagerId, null, now);

        listener.handleEmployeeManagerChanged(event);

        ArgumentCaptor<OrganizationAuditLog> captor = ArgumentCaptor.forClass(OrganizationAuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        OrganizationAuditLog log = captor.getValue();
        assertEquals(orgId, log.getOrganizationId());
        assertEquals("EMPLOYEE_MANAGER_CHANGED", log.getAction());
        assertTrue(log.getOldValues().contains("\"managerId\":101"));
        assertTrue(log.getNewValues().contains("\"managerId\":null"));
    }

    // ── Case 6: EmployeeSuspendedEvent ───────────────────────────────────────
    @Test
    @DisplayName("Case 6: handleEmployeeSuspended records EMPLOYEE_SUSPENDED with suspension reason")
    void testHandleEmployeeSuspended_AuditLogged() {
        EmployeeSuspendedEvent event = new EmployeeSuspendedEvent(employeeId, orgId, "Policy Violation", now);

        listener.handleEmployeeSuspended(event);

        ArgumentCaptor<OrganizationAuditLog> captor = ArgumentCaptor.forClass(OrganizationAuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        OrganizationAuditLog log = captor.getValue();
        assertEquals(orgId, log.getOrganizationId());
        assertEquals("EMPLOYEE_SUSPENDED", log.getAction());
        assertEquals(employeeId, log.getEntityId());
        assertNull(log.getOldValues());
        assertTrue(log.getNewValues().contains("\"status\":\"SUSPENDED\""));
        assertTrue(log.getNewValues().contains("\"reason\":\"Policy Violation\""));
    }

    // ── Case 7: EmployeeTerminatedEvent ──────────────────────────────────────
    @Test
    @DisplayName("Case 7: handleEmployeeTerminated records EMPLOYEE_TERMINATED with termination reason")
    void testHandleEmployeeTerminated_AuditLogged() {
        EmployeeTerminatedEvent event = new EmployeeTerminatedEvent(employeeId, orgId, "Contract Ended", now);

        listener.handleEmployeeTerminated(event);

        ArgumentCaptor<OrganizationAuditLog> captor = ArgumentCaptor.forClass(OrganizationAuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        OrganizationAuditLog log = captor.getValue();
        assertEquals(orgId, log.getOrganizationId());
        assertEquals("EMPLOYEE_TERMINATED", log.getAction());
        assertEquals(employeeId, log.getEntityId());
        assertNull(log.getOldValues());
        assertTrue(log.getNewValues().contains("\"status\":\"INACTIVE\""));
        assertTrue(log.getNewValues().contains("\"reason\":\"Contract Ended\""));
    }

    // ── Case 8: PerformedBy Actor from SecurityContext ───────────────────────
    @Test
    @DisplayName("Case 8: Actor is resolved from authenticated SecurityContext principal")
    void testActorResolution_FromSecurityContext() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "hr.admin@company.com", "credentials", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        EmployeeActivatedEvent event = new EmployeeActivatedEvent(employeeId, orgId, now);
        listener.handleEmployeeActivated(event);

        ArgumentCaptor<OrganizationAuditLog> captor = ArgumentCaptor.forClass(OrganizationAuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertEquals("hr.admin@company.com", captor.getValue().getPerformedBy());
    }

    // ── Case 9: PerformedBy Actor Fallback to SYSTEM ─────────────────────────
    @Test
    @DisplayName("Case 9: Actor falls back to SYSTEM when no authenticated principal exists")
    void testActorResolution_FallbackSystem() {
        SecurityContextHolder.clearContext();

        EmployeeActivatedEvent event = new EmployeeActivatedEvent(employeeId, orgId, now);
        listener.handleEmployeeActivated(event);

        ArgumentCaptor<OrganizationAuditLog> captor = ArgumentCaptor.forClass(OrganizationAuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertEquals("SYSTEM", captor.getValue().getPerformedBy());
    }

    // ── Case 10: Organization Resolution from Employee Record ───────────────
    @Test
    @DisplayName("Case 10: Resolves organizationId from employee record when event.organizationId is null")
    void testOrganizationResolution_FromEmployeeRecord() {
        Organization org = new Organization();
        org.setId(99L);
        Employee emp = new Employee();
        emp.setId(employeeId);
        emp.setOrganization(org);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(emp));

        EmployeeActivatedEvent event = new EmployeeActivatedEvent(employeeId, null, now);
        listener.handleEmployeeActivated(event);

        ArgumentCaptor<OrganizationAuditLog> captor = ArgumentCaptor.forClass(OrganizationAuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertEquals(99L, captor.getValue().getOrganizationId());
    }

    // ── Case 11: Missing Organization Safely Skips ───────────────────────────
    @Test
    @DisplayName("Case 11: Safely skips audit logging when organizationId cannot be resolved")
    void testOrganizationResolution_NotFound_SafelySkips() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        EmployeeActivatedEvent event = new EmployeeActivatedEvent(employeeId, null, now);
        assertDoesNotThrow(() -> listener.handleEmployeeActivated(event));

        verify(auditLogRepository, never()).save(any());
    }

    // ── Case 12: Null Event or Null EmployeeId Safely No-Ops ─────────────────
    @Test
    @DisplayName("Case 12: Safely no-ops when event or employeeId is null")
    void testNullEventHandling_SafelyNoOps() {
        assertDoesNotThrow(() -> listener.handleEmployeeJoined(null));
        assertDoesNotThrow(() -> listener.handleEmployeeActivated(new EmployeeActivatedEvent(null, orgId, now)));
        assertDoesNotThrow(() -> listener.handleEmployeeSuspended(null));
        assertDoesNotThrow(() -> listener.handleEmployeeTerminated(null));
        assertDoesNotThrow(() -> listener.handleEmployeeTransferred(null));
        assertDoesNotThrow(() -> listener.handleEmployeeManagerChanged(null));

        verifyNoInteractions(auditLogRepository);
    }
}
