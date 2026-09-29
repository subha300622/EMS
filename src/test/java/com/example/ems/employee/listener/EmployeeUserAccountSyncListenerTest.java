package com.example.ems.employee.listener;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.SessionService;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.event.*;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.service.EmployeeUserAccountSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeUserAccountSyncListenerTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private SessionService sessionService;

    private EmployeeUserAccountSyncService syncService;
    private EmployeeUserAccountSyncListener listener;

    private final Long orgId = 1L;
    private final Long employeeId = 100L;
    private final String email = "alice@company.com";
    private final String userId = "EMP100";

    private Employee employee;
    private User user;

    @BeforeEach
    void setUp() {
        syncService = new EmployeeUserAccountSyncService(
                employeeRepository,
                userRepository,
                departmentRepository,
                sessionService
        );
        listener = new EmployeeUserAccountSyncListener(syncService);

        employee = new Employee();
        employee.setId(employeeId);
        employee.setEmail(email);

        user = new User();
        user.setId(500L);
        user.setUserId(userId);
        user.setWorkEmail(email);
        user.setOrganizationId(orgId);
        user.setStatus("ACTIVE");
    }

    // ── Case 1: Joined → no-op ───────────────────────────────────────────────
    @Test
    @DisplayName("Case 1: handleEmployeeJoined is a complete no-op with zero repository interactions")
    void testHandleEmployeeJoined_NoOp() {
        EmployeeJoinedEvent event = new EmployeeJoinedEvent(employeeId, orgId, Instant.now());

        listener.handleEmployeeJoined(event);

        verifyNoInteractions(employeeRepository);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(sessionService);
        verifyNoInteractions(departmentRepository);
    }

    // ── Case 2: Activated → User.status = ACTIVE ─────────────────────────────
    @Test
    @DisplayName("Case 2: handleEmployeeActivated updates User.status to ACTIVE")
    void testHandleEmployeeActivated_UpdatesStatusToActive() {
        user.setStatus("SUSPENDED");
        when(employeeRepository.findByIdAndOrganizationId(employeeId, orgId)).thenReturn(Optional.of(employee));
        when(userRepository.findByWorkEmailAndOrganizationId(email, orgId)).thenReturn(Optional.of(user));

        EmployeeActivatedEvent event = new EmployeeActivatedEvent(employeeId, orgId, Instant.now());
        listener.handleEmployeeActivated(event);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("ACTIVE", captor.getValue().getStatus());
        verifyNoInteractions(sessionService);
    }

    // ── Case 3: Activated → no User account → safely no-ops ──────────────────
    @Test
    @DisplayName("Case 3: handleEmployeeActivated safely no-ops when no User account exists")
    void testHandleEmployeeActivated_NoUser_SafelyNoOps() {
        when(employeeRepository.findByIdAndOrganizationId(employeeId, orgId)).thenReturn(Optional.of(employee));
        when(userRepository.findByWorkEmailAndOrganizationId(email, orgId)).thenReturn(Optional.empty());
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.empty());

        EmployeeActivatedEvent event = new EmployeeActivatedEvent(employeeId, orgId, Instant.now());
        assertDoesNotThrow(() -> listener.handleEmployeeActivated(event));

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(sessionService);
    }

    // ── Case 4: Suspended → User.status = SUSPENDED & revokeAllSessions ─────
    @Test
    @DisplayName("Case 4: handleEmployeeSuspended updates User.status to SUSPENDED and revokes sessions")
    void testHandleEmployeeSuspended_UpdatesStatusToSuspendedAndRevokesSessions() {
        when(employeeRepository.findByIdAndOrganizationId(employeeId, orgId)).thenReturn(Optional.of(employee));
        when(userRepository.findByWorkEmailAndOrganizationId(email, orgId)).thenReturn(Optional.of(user));

        EmployeeSuspendedEvent event = new EmployeeSuspendedEvent(employeeId, orgId, "Investigation", Instant.now());
        listener.handleEmployeeSuspended(event);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("SUSPENDED", captor.getValue().getStatus());
        verify(sessionService).revokeAllSessions(userId);
    }

    // ── Case 5: Suspended → no User account → safely no-ops ──────────────────
    @Test
    @DisplayName("Case 5: handleEmployeeSuspended safely no-ops when no User account exists")
    void testHandleEmployeeSuspended_NoUser_SafelyNoOps() {
        when(employeeRepository.findByIdAndOrganizationId(employeeId, orgId)).thenReturn(Optional.of(employee));
        when(userRepository.findByWorkEmailAndOrganizationId(email, orgId)).thenReturn(Optional.empty());
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.empty());

        EmployeeSuspendedEvent event = new EmployeeSuspendedEvent(employeeId, orgId, "Investigation", Instant.now());
        assertDoesNotThrow(() -> listener.handleEmployeeSuspended(event));

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(sessionService);
    }

    // ── Case 6: Terminated → User.status = INACTIVE & revokeAllSessions ─────
    @Test
    @DisplayName("Case 6: handleEmployeeTerminated updates User.status to INACTIVE and revokes sessions")
    void testHandleEmployeeTerminated_UpdatesStatusToInactiveAndRevokesSessions() {
        when(employeeRepository.findByIdAndOrganizationId(employeeId, orgId)).thenReturn(Optional.of(employee));
        when(userRepository.findByWorkEmailAndOrganizationId(email, orgId)).thenReturn(Optional.of(user));

        EmployeeTerminatedEvent event = new EmployeeTerminatedEvent(employeeId, orgId, "Resigned", Instant.now());
        listener.handleEmployeeTerminated(event);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("INACTIVE", captor.getValue().getStatus());
        verify(sessionService).revokeAllSessions(userId);
    }

    // ── Case 7: Terminated → no User account → safely no-ops ─────────────────
    @Test
    @DisplayName("Case 7: handleEmployeeTerminated safely no-ops when no User account exists")
    void testHandleEmployeeTerminated_NoUser_SafelyNoOps() {
        when(employeeRepository.findByIdAndOrganizationId(employeeId, orgId)).thenReturn(Optional.of(employee));
        when(userRepository.findByWorkEmailAndOrganizationId(email, orgId)).thenReturn(Optional.empty());
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.empty());

        EmployeeTerminatedEvent event = new EmployeeTerminatedEvent(employeeId, orgId, "Resigned", Instant.now());
        assertDoesNotThrow(() -> listener.handleEmployeeTerminated(event));

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(sessionService);
    }

    // ── Case 8: Transferred → updates departmentId and department name ───────
    @Test
    @DisplayName("Case 8: handleEmployeeTransferred updates User.departmentId and User.department")
    void testHandleEmployeeTransferred_UpdatesDepartment() {
        Long targetDeptId = 20L;
        Department targetDept = new Department();
        targetDept.setId(targetDeptId);
        targetDept.setName("Engineering");

        when(employeeRepository.findByIdAndOrganizationId(employeeId, orgId)).thenReturn(Optional.of(employee));
        when(userRepository.findByWorkEmailAndOrganizationId(email, orgId)).thenReturn(Optional.of(user));
        when(departmentRepository.findByIdAndOrganizationId(targetDeptId, orgId)).thenReturn(Optional.of(targetDept));

        EmployeeTransferredEvent event = new EmployeeTransferredEvent(employeeId, orgId, 10L, targetDeptId, Instant.now());
        listener.handleEmployeeTransferred(event);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(targetDeptId, captor.getValue().getDepartmentId());
        assertEquals("Engineering", captor.getValue().getDepartment());
    }

    // ── Case 9: Transferred → no User account → safely no-ops ────────────────
    @Test
    @DisplayName("Case 9: handleEmployeeTransferred safely no-ops when no User account exists")
    void testHandleEmployeeTransferred_NoUser_SafelyNoOps() {
        when(employeeRepository.findByIdAndOrganizationId(employeeId, orgId)).thenReturn(Optional.of(employee));
        when(userRepository.findByWorkEmailAndOrganizationId(email, orgId)).thenReturn(Optional.empty());
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.empty());

        EmployeeTransferredEvent event = new EmployeeTransferredEvent(employeeId, orgId, 10L, 20L, Instant.now());
        assertDoesNotThrow(() -> listener.handleEmployeeTransferred(event));

        verify(userRepository, never()).save(any(User.class));
    }

    // ── Case 10: ManagerChanged with new manager → reportingManagerId set ────
    @Test
    @DisplayName("Case 10: handleEmployeeManagerChanged updates reportingManagerId to new manager's User.id")
    void testHandleEmployeeManagerChanged_WithNewManager_UpdatesReportingManagerId() {
        Long newManagerEmpId = 200L;
        String managerEmail = "bob.manager@company.com";
        Employee managerEmp = new Employee();
        managerEmp.setId(newManagerEmpId);
        managerEmp.setEmail(managerEmail);

        User managerUser = new User();
        managerUser.setId(600L);
        managerUser.setWorkEmail(managerEmail);
        managerUser.setOrganizationId(orgId);

        when(employeeRepository.findByIdAndOrganizationId(employeeId, orgId)).thenReturn(Optional.of(employee));
        when(userRepository.findByWorkEmailAndOrganizationId(email, orgId)).thenReturn(Optional.of(user));
        when(employeeRepository.findByIdAndOrganizationId(newManagerEmpId, orgId)).thenReturn(Optional.of(managerEmp));
        when(userRepository.findByWorkEmailAndOrganizationId(managerEmail, orgId)).thenReturn(Optional.of(managerUser));

        EmployeeManagerChangedEvent event = new EmployeeManagerChangedEvent(employeeId, orgId, 99L, newManagerEmpId, Instant.now());
        listener.handleEmployeeManagerChanged(event);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(600L, captor.getValue().getReportingManagerId());
    }

    // ── Case 11: ManagerChanged → manager has no User account → sets null ───
    @Test
    @DisplayName("Case 11: handleEmployeeManagerChanged sets reportingManagerId to null when manager has no User account")
    void testHandleEmployeeManagerChanged_NewManagerHasNoUser_SetsNull() {
        Long newManagerEmpId = 200L;
        String managerEmail = "bob.manager@company.com";
        Employee managerEmp = new Employee();
        managerEmp.setId(newManagerEmpId);
        managerEmp.setEmail(managerEmail);

        user.setReportingManagerId(999L); // previous manager

        when(employeeRepository.findByIdAndOrganizationId(employeeId, orgId)).thenReturn(Optional.of(employee));
        when(userRepository.findByWorkEmailAndOrganizationId(email, orgId)).thenReturn(Optional.of(user));
        when(employeeRepository.findByIdAndOrganizationId(newManagerEmpId, orgId)).thenReturn(Optional.of(managerEmp));
        when(userRepository.findByWorkEmailAndOrganizationId(managerEmail, orgId)).thenReturn(Optional.empty());
        when(userRepository.findByWorkEmail(managerEmail)).thenReturn(Optional.empty());

        EmployeeManagerChangedEvent event = new EmployeeManagerChangedEvent(employeeId, orgId, 99L, newManagerEmpId, Instant.now());
        listener.handleEmployeeManagerChanged(event);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertNull(captor.getValue().getReportingManagerId());
    }

    // ── Case 12: ManagerChanged → manager removed (null) → sets null ─────────
    @Test
    @DisplayName("Case 12: handleEmployeeManagerChanged sets reportingManagerId to null when manager is removed")
    void testHandleEmployeeManagerChanged_ManagerRemoved_SetsNull() {
        user.setReportingManagerId(999L); // previous manager

        when(employeeRepository.findByIdAndOrganizationId(employeeId, orgId)).thenReturn(Optional.of(employee));
        when(userRepository.findByWorkEmailAndOrganizationId(email, orgId)).thenReturn(Optional.of(user));

        EmployeeManagerChangedEvent event = new EmployeeManagerChangedEvent(employeeId, orgId, 99L, null, Instant.now());
        listener.handleEmployeeManagerChanged(event);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertNull(captor.getValue().getReportingManagerId());
    }

    // ── Case 13: ManagerChanged → employee has no User account → no-ops ──────
    @Test
    @DisplayName("Case 13: handleEmployeeManagerChanged safely no-ops when employee has no User account")
    void testHandleEmployeeManagerChanged_NoUser_SafelyNoOps() {
        when(employeeRepository.findByIdAndOrganizationId(employeeId, orgId)).thenReturn(Optional.of(employee));
        when(userRepository.findByWorkEmailAndOrganizationId(email, orgId)).thenReturn(Optional.empty());
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.empty());

        EmployeeManagerChangedEvent event = new EmployeeManagerChangedEvent(employeeId, orgId, 99L, 200L, Instant.now());
        assertDoesNotThrow(() -> listener.handleEmployeeManagerChanged(event));

        verify(userRepository, never()).save(any(User.class));
    }
}
