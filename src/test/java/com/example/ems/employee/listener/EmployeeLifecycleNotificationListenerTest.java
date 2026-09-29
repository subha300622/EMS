package com.example.ems.employee.listener;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.repository.NotificationRepository;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.event.*;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.service.EmployeeLifecycleNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeLifecycleNotificationListenerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    private EmployeeLifecycleNotificationService notificationService;
    private EmployeeLifecycleNotificationListener listener;

    private final Long orgId = 1L;
    private final Long employeeId = 100L;
    private final String employeeEmail = "alice@company.com";
    private final Long employeeUserId = 501L;

    private final Long managerId = 200L;
    private final String managerEmail = "bob.manager@company.com";
    private final Long managerUserId = 601L;

    private Employee employee;
    private Employee manager;
    private User employeeUser;
    private User managerUser;

    @BeforeEach
    void setUp() {
        notificationService = new EmployeeLifecycleNotificationService(
                notificationRepository,
                employeeRepository,
                userRepository,
                departmentRepository
        );
        listener = new EmployeeLifecycleNotificationListener(notificationService);

        manager = new Employee();
        manager.setId(managerId);
        manager.setFullName("Bob Manager");
        manager.setEmail(managerEmail);

        managerUser = new User();
        managerUser.setId(managerUserId);
        managerUser.setWorkEmail(managerEmail);
        managerUser.setOrganizationId(orgId);

        employee = new Employee();
        employee.setId(employeeId);
        employee.setFullName("Alice Employee");
        employee.setEmail(employeeEmail);
        employee.setManager(manager);

        employeeUser = new User();
        employeeUser.setId(employeeUserId);
        employeeUser.setWorkEmail(employeeEmail);
        employeeUser.setOrganizationId(orgId);
    }

    // ── Case 1: EmployeeJoinedEvent ──────────────────────────────────────────
    @Test
    @DisplayName("Case 1: handleEmployeeJoined notifies both employee and manager when User accounts exist")
    void testHandleEmployeeJoined_NotifiesEmployeeAndManager() {
        when(employeeRepository.findByIdAndOrganizationId(employeeId, orgId)).thenReturn(Optional.of(employee));
        when(userRepository.findByWorkEmailAndOrganizationId(employeeEmail, orgId)).thenReturn(Optional.of(employeeUser));
        when(employeeRepository.findByIdAndOrganizationId(managerId, orgId)).thenReturn(Optional.of(manager));
        when(userRepository.findByWorkEmailAndOrganizationId(managerEmail, orgId)).thenReturn(Optional.of(managerUser));
        when(notificationRepository.existsByIdempotencyKey(anyString())).thenReturn(false);

        EmployeeJoinedEvent event = new EmployeeJoinedEvent(employeeId, orgId, Instant.now());
        listener.handleEmployeeJoined(event);

        // Verify notification sent to employee
        verify(notificationRepository).insertNotificationIfNotExists(
                eq(employeeUserId),
                eq("Welcome to the Organization"),
                contains("Welcome!"),
                eq("SYSTEM"),
                eq("MEDIUM"),
                eq(false),
                any(LocalDateTime.class),
                eq(String.format("LIFECYCLE:JOINED:%d:%d", employeeId, employeeUserId))
        );

        // Verify notification sent to manager
        verify(notificationRepository).insertNotificationIfNotExists(
                eq(managerUserId),
                eq("New Team Member Joined"),
                contains("Alice Employee"),
                eq("SYSTEM"),
                eq("MEDIUM"),
                eq(false),
                any(LocalDateTime.class),
                eq(String.format("LIFECYCLE:JOINED:MGR:%d:%d", employeeId, managerId))
        );
    }

    // ── Case 2: EmployeeJoinedEvent (No User Account) ────────────────────────
    @Test
    @DisplayName("Case 2: handleEmployeeJoined safely no-ops when employee and manager have no User accounts")
    void testHandleEmployeeJoined_NoUser_SafelyNoOps() {
        employee.setManager(null);
        when(employeeRepository.findByIdAndOrganizationId(employeeId, orgId)).thenReturn(Optional.of(employee));
        when(userRepository.findByWorkEmailAndOrganizationId(employeeEmail, orgId)).thenReturn(Optional.empty());
        when(userRepository.findByWorkEmail(employeeEmail)).thenReturn(Optional.empty());

        EmployeeJoinedEvent event = new EmployeeJoinedEvent(employeeId, orgId, Instant.now());
        assertDoesNotThrow(() -> listener.handleEmployeeJoined(event));

        verify(notificationRepository, never()).insertNotificationIfNotExists(any(), any(), any(), any(), any(), anyBoolean(), any(), any());
    }

    // ── Case 3: EmployeeActivatedEvent ───────────────────────────────────────
    @Test
    @DisplayName("Case 3: handleEmployeeActivated notifies employee with Account Activated")
    void testHandleEmployeeActivated_NotifiesEmployee() {
        when(employeeRepository.findByIdAndOrganizationId(employeeId, orgId)).thenReturn(Optional.of(employee));
        when(userRepository.findByWorkEmailAndOrganizationId(employeeEmail, orgId)).thenReturn(Optional.of(employeeUser));
        when(notificationRepository.existsByIdempotencyKey(anyString())).thenReturn(false);

        EmployeeActivatedEvent event = new EmployeeActivatedEvent(employeeId, orgId, Instant.now());
        listener.handleEmployeeActivated(event);

        verify(notificationRepository).insertNotificationIfNotExists(
                eq(employeeUserId),
                eq("Account Activated"),
                contains("activated"),
                eq("SYSTEM"),
                eq("MEDIUM"),
                eq(false),
                any(LocalDateTime.class),
                eq(String.format("LIFECYCLE:ACTIVATED:%d:%d", employeeId, employeeUserId))
        );
    }

    // ── Case 4: EmployeeActivatedEvent (No User Account) ─────────────────────
    @Test
    @DisplayName("Case 4: handleEmployeeActivated safely no-ops when employee has no User account")
    void testHandleEmployeeActivated_NoUser_SafelyNoOps() {
        when(employeeRepository.findByIdAndOrganizationId(employeeId, orgId)).thenReturn(Optional.of(employee));
        when(userRepository.findByWorkEmailAndOrganizationId(employeeEmail, orgId)).thenReturn(Optional.empty());
        when(userRepository.findByWorkEmail(employeeEmail)).thenReturn(Optional.empty());

        EmployeeActivatedEvent event = new EmployeeActivatedEvent(employeeId, orgId, Instant.now());
        assertDoesNotThrow(() -> listener.handleEmployeeActivated(event));

        verify(notificationRepository, never()).insertNotificationIfNotExists(any(), any(), any(), any(), any(), anyBoolean(), any(), any());
    }

    // ── Case 5: EmployeeTransferredEvent ─────────────────────────────────────
    @Test
    @DisplayName("Case 5: handleEmployeeTransferred notifies employee and manager of department transfer")
    void testHandleEmployeeTransferred_NotifiesEmployeeAndManager() {
        Long targetDeptId = 15L;
        Department targetDept = new Department();
        targetDept.setId(targetDeptId);
        targetDept.setName("Product Design");

        when(employeeRepository.findByIdAndOrganizationId(employeeId, orgId)).thenReturn(Optional.of(employee));
        when(departmentRepository.findByIdAndOrganizationId(targetDeptId, orgId)).thenReturn(Optional.of(targetDept));
        when(userRepository.findByWorkEmailAndOrganizationId(employeeEmail, orgId)).thenReturn(Optional.of(employeeUser));
        when(employeeRepository.findByIdAndOrganizationId(managerId, orgId)).thenReturn(Optional.of(manager));
        when(userRepository.findByWorkEmailAndOrganizationId(managerEmail, orgId)).thenReturn(Optional.of(managerUser));
        when(notificationRepository.existsByIdempotencyKey(anyString())).thenReturn(false);

        EmployeeTransferredEvent event = new EmployeeTransferredEvent(employeeId, orgId, 10L, targetDeptId, Instant.now());
        listener.handleEmployeeTransferred(event);

        verify(notificationRepository).insertNotificationIfNotExists(
                eq(employeeUserId),
                eq("Department Transfer Completed"),
                contains("Product Design"),
                eq("SYSTEM"),
                eq("MEDIUM"),
                eq(false),
                any(LocalDateTime.class),
                eq(String.format("LIFECYCLE:TRANSFERRED:%d:%d:%d", employeeId, targetDeptId, employeeUserId))
        );

        verify(notificationRepository).insertNotificationIfNotExists(
                eq(managerUserId),
                eq("Department Transfer"),
                contains("Alice Employee"),
                eq("SYSTEM"),
                eq("MEDIUM"),
                eq(false),
                any(LocalDateTime.class),
                eq(String.format("LIFECYCLE:TRANSFERRED:MGR:%d:%d:%d", employeeId, targetDeptId, managerId))
        );
    }

    // ── Case 6: EmployeeTransferredEvent (No User Account) ───────────────────
    @Test
    @DisplayName("Case 6: handleEmployeeTransferred safely no-ops when employee and manager have no User accounts")
    void testHandleEmployeeTransferred_NoUser_SafelyNoOps() {
        employee.setManager(null);
        when(employeeRepository.findByIdAndOrganizationId(employeeId, orgId)).thenReturn(Optional.of(employee));
        when(userRepository.findByWorkEmailAndOrganizationId(employeeEmail, orgId)).thenReturn(Optional.empty());
        when(userRepository.findByWorkEmail(employeeEmail)).thenReturn(Optional.empty());

        EmployeeTransferredEvent event = new EmployeeTransferredEvent(employeeId, orgId, 10L, 15L, Instant.now());
        assertDoesNotThrow(() -> listener.handleEmployeeTransferred(event));

        verify(notificationRepository, never()).insertNotificationIfNotExists(any(), any(), any(), any(), any(), anyBoolean(), any(), any());
    }

    // ── Case 7: EmployeeManagerChangedEvent ──────────────────────────────────
    @Test
    @DisplayName("Case 7: handleEmployeeManagerChanged notifies employee, new manager, and old manager")
    void testHandleEmployeeManagerChanged_NotifiesEmployeeNewManagerAndOldManager() {
        Long oldMgrId = 111L;
        Long newMgrId = 222L;

        Employee oldMgrEmp = new Employee();
        oldMgrEmp.setId(oldMgrId);
        oldMgrEmp.setFullName("Old Manager");
        oldMgrEmp.setEmail("old.mgr@company.com");
        User oldMgrUser = new User();
        oldMgrUser.setId(701L);

        Employee newMgrEmp = new Employee();
        newMgrEmp.setId(newMgrId);
        newMgrEmp.setFullName("New Manager");
        newMgrEmp.setEmail("new.mgr@company.com");
        User newMgrUser = new User();
        newMgrUser.setId(801L);

        when(employeeRepository.findByIdAndOrganizationId(employeeId, orgId)).thenReturn(Optional.of(employee));
        when(userRepository.findByWorkEmailAndOrganizationId(employeeEmail, orgId)).thenReturn(Optional.of(employeeUser));

        when(employeeRepository.findByIdAndOrganizationId(newMgrId, orgId)).thenReturn(Optional.of(newMgrEmp));
        when(userRepository.findByWorkEmailAndOrganizationId("new.mgr@company.com", orgId)).thenReturn(Optional.of(newMgrUser));

        when(employeeRepository.findByIdAndOrganizationId(oldMgrId, orgId)).thenReturn(Optional.of(oldMgrEmp));
        when(userRepository.findByWorkEmailAndOrganizationId("old.mgr@company.com", orgId)).thenReturn(Optional.of(oldMgrUser));

        when(notificationRepository.existsByIdempotencyKey(anyString())).thenReturn(false);

        EmployeeManagerChangedEvent event = new EmployeeManagerChangedEvent(employeeId, orgId, oldMgrId, newMgrId, Instant.now());
        listener.handleEmployeeManagerChanged(event);

        // Employee notification
        verify(notificationRepository).insertNotificationIfNotExists(
                eq(employeeUserId), eq("Manager Assignment Updated"), anyString(), eq("SYSTEM"), eq("MEDIUM"), eq(false), any(), anyString());

        // New manager notification
        verify(notificationRepository).insertNotificationIfNotExists(
                eq(801L), eq("New Direct Report Assigned"), contains("Alice Employee"), eq("SYSTEM"), eq("MEDIUM"), eq(false), any(), anyString());

        // Old manager notification
        verify(notificationRepository).insertNotificationIfNotExists(
                eq(701L), eq("Direct Report Reassigned"), contains("Alice Employee"), eq("SYSTEM"), eq("MEDIUM"), eq(false), any(), anyString());
    }

    // ── Case 8: EmployeeManagerChangedEvent (Manager Removed) ─────────────────
    @Test
    @DisplayName("Case 8: handleEmployeeManagerChanged with manager removed notifies employee and old manager only")
    void testHandleEmployeeManagerChanged_ManagerRemoved_OnlyNotifiesEmployeeAndOldManager() {
        Long oldMgrId = 111L;
        Employee oldMgrEmp = new Employee();
        oldMgrEmp.setId(oldMgrId);
        oldMgrEmp.setFullName("Old Manager");
        oldMgrEmp.setEmail("old.mgr@company.com");
        User oldMgrUser = new User();
        oldMgrUser.setId(701L);

        when(employeeRepository.findByIdAndOrganizationId(employeeId, orgId)).thenReturn(Optional.of(employee));
        when(userRepository.findByWorkEmailAndOrganizationId(employeeEmail, orgId)).thenReturn(Optional.of(employeeUser));
        when(employeeRepository.findByIdAndOrganizationId(oldMgrId, orgId)).thenReturn(Optional.of(oldMgrEmp));
        when(userRepository.findByWorkEmailAndOrganizationId("old.mgr@company.com", orgId)).thenReturn(Optional.of(oldMgrUser));
        when(notificationRepository.existsByIdempotencyKey(anyString())).thenReturn(false);

        EmployeeManagerChangedEvent event = new EmployeeManagerChangedEvent(employeeId, orgId, oldMgrId, null, Instant.now());
        listener.handleEmployeeManagerChanged(event);

        verify(notificationRepository).insertNotificationIfNotExists(
                eq(employeeUserId), eq("Manager Assignment Updated"), anyString(), eq("SYSTEM"), eq("MEDIUM"), eq(false), any(), anyString());

        verify(notificationRepository).insertNotificationIfNotExists(
                eq(701L), eq("Direct Report Reassigned"), anyString(), eq("SYSTEM"), eq("MEDIUM"), eq(false), any(), anyString());
    }

    // ── Case 9: EmployeeSuspendedEvent ───────────────────────────────────────
    @Test
    @DisplayName("Case 9: handleEmployeeSuspended notifies employee and manager with HIGH priority and reason")
    void testHandleEmployeeSuspended_NotifiesEmployeeAndManager() {
        when(employeeRepository.findByIdAndOrganizationId(employeeId, orgId)).thenReturn(Optional.of(employee));
        when(userRepository.findByWorkEmailAndOrganizationId(employeeEmail, orgId)).thenReturn(Optional.of(employeeUser));
        when(employeeRepository.findByIdAndOrganizationId(managerId, orgId)).thenReturn(Optional.of(manager));
        when(userRepository.findByWorkEmailAndOrganizationId(managerEmail, orgId)).thenReturn(Optional.of(managerUser));
        when(notificationRepository.existsByIdempotencyKey(anyString())).thenReturn(false);

        EmployeeSuspendedEvent event = new EmployeeSuspendedEvent(employeeId, orgId, "Policy breach", Instant.now());
        listener.handleEmployeeSuspended(event);

        verify(notificationRepository).insertNotificationIfNotExists(
                eq(employeeUserId),
                eq("Account Suspended"),
                contains("Policy breach"),
                eq("SYSTEM"),
                eq("HIGH"),
                eq(false),
                any(LocalDateTime.class),
                eq(String.format("LIFECYCLE:SUSPENDED:%d:%d", employeeId, employeeUserId))
        );

        verify(notificationRepository).insertNotificationIfNotExists(
                eq(managerUserId),
                eq("Team Member Suspended"),
                contains("Alice Employee"),
                eq("SYSTEM"),
                eq("MEDIUM"),
                eq(false),
                any(LocalDateTime.class),
                eq(String.format("LIFECYCLE:SUSPENDED:MGR:%d:%d", employeeId, managerId))
        );
    }

    // ── Case 10: EmployeeTerminatedEvent ─────────────────────────────────────
    @Test
    @DisplayName("Case 10: handleEmployeeTerminated notifies employee and manager with HIGH priority and reason")
    void testHandleEmployeeTerminated_NotifiesEmployeeAndManager() {
        when(employeeRepository.findByIdAndOrganizationId(employeeId, orgId)).thenReturn(Optional.of(employee));
        when(userRepository.findByWorkEmailAndOrganizationId(employeeEmail, orgId)).thenReturn(Optional.of(employeeUser));
        when(employeeRepository.findByIdAndOrganizationId(managerId, orgId)).thenReturn(Optional.of(manager));
        when(userRepository.findByWorkEmailAndOrganizationId(managerEmail, orgId)).thenReturn(Optional.of(managerUser));
        when(notificationRepository.existsByIdempotencyKey(anyString())).thenReturn(false);

        EmployeeTerminatedEvent event = new EmployeeTerminatedEvent(employeeId, orgId, "Voluntary resignation", Instant.now());
        listener.handleEmployeeTerminated(event);

        verify(notificationRepository).insertNotificationIfNotExists(
                eq(employeeUserId),
                eq("Employment Terminated"),
                contains("Voluntary resignation"),
                eq("SYSTEM"),
                eq("HIGH"),
                eq(false),
                any(LocalDateTime.class),
                eq(String.format("LIFECYCLE:TERMINATED:%d:%d", employeeId, employeeUserId))
        );

        verify(notificationRepository).insertNotificationIfNotExists(
                eq(managerUserId),
                eq("Team Member Terminated"),
                contains("Alice Employee"),
                eq("SYSTEM"),
                eq("MEDIUM"),
                eq(false),
                any(LocalDateTime.class),
                eq(String.format("LIFECYCLE:TERMINATED:MGR:%d:%d", employeeId, managerId))
        );
    }

    // ── Case 11: Idempotency Key Prevents Duplicate Notifications ────────────
    @Test
    @DisplayName("Case 11: Duplicate notification prevented when idempotencyKey already exists in repository")
    void testDuplicateNotification_IdempotencyKey_PreventsDuplicate() {
        when(employeeRepository.findByIdAndOrganizationId(employeeId, orgId)).thenReturn(Optional.of(employee));
        when(userRepository.findByWorkEmailAndOrganizationId(employeeEmail, orgId)).thenReturn(Optional.of(employeeUser));
        when(notificationRepository.existsByIdempotencyKey(anyString())).thenReturn(true);

        EmployeeActivatedEvent event = new EmployeeActivatedEvent(employeeId, orgId, Instant.now());
        listener.handleEmployeeActivated(event);

        // insertNotificationIfNotExists should not be called because existsByIdempotencyKey was true
        verify(notificationRepository, never()).insertNotificationIfNotExists(any(), any(), any(), any(), any(), anyBoolean(), any(), any());
    }

    // ── Case 12: Null Event Handling Safely No-Ops ───────────────────────────
    @Test
    @DisplayName("Case 12: Safely no-ops on null events or null employeeId")
    void testNullEventHandling_SafelyNoOps() {
        assertDoesNotThrow(() -> listener.handleEmployeeJoined(null));
        assertDoesNotThrow(() -> listener.handleEmployeeActivated(new EmployeeActivatedEvent(null, orgId, Instant.now())));
        assertDoesNotThrow(() -> listener.handleEmployeeSuspended(null));
        assertDoesNotThrow(() -> listener.handleEmployeeTerminated(null));
        assertDoesNotThrow(() -> listener.handleEmployeeTransferred(null));
        assertDoesNotThrow(() -> listener.handleEmployeeManagerChanged(null));

        verifyNoInteractions(notificationRepository);
    }
}
