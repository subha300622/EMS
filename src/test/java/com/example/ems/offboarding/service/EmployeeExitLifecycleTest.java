package com.example.ems.offboarding.service;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.approval.event.ApprovalWorkflowRejectedEvent;
import com.example.ems.approval.service.ApprovalWorkflowEngineService;
import com.example.ems.auth.entity.User;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.offboarding.dto.*;
import com.example.ems.offboarding.entity.EmployeeExit;
import com.example.ems.offboarding.entity.ExitClearance;
import com.example.ems.offboarding.repository.EmployeeExitRepository;
import com.example.ems.offboarding.repository.ExitClearanceRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeExitLifecycleTest {

    @Mock
    private EmployeeExitRepository exitRepository;

    @Mock
    private ExitClearanceRepository clearanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ApprovalWorkflowEngineService approvalWorkflowEngineService;

    @Mock
    private ExitAuthorizationService exitAuthService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private EmployeeExitService exitService;

    @InjectMocks
    private ExitClearanceService clearanceService;

    @InjectMocks
    private ExitWorkflowEventListener eventListener;

    private Organization testOrg;
    private Employee resigningEmployee;
    private Employee managerEmployee;
    private Employee itOfficer;
    private Employee adminOfficer;
    private Employee financeOfficer;
    private User hrUser;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(10L);

        testOrg = new Organization();
        testOrg.setId(10L);
        testOrg.setName("Acme Corporation");

        managerEmployee = new Employee();
        managerEmployee.setId(200L);
        managerEmployee.setFullName("John Manager");
        managerEmployee.setEmail("manager@acme.com");
        managerEmployee.setEmployeeId("EMP-200");
        managerEmployee.setOrganization(testOrg);
        managerEmployee.setStatus("ACTIVE");

        resigningEmployee = new Employee();
        resigningEmployee.setId(101L);
        resigningEmployee.setFullName("Alice Developer");
        resigningEmployee.setEmail("alice@acme.com");
        resigningEmployee.setEmployeeId("EMP-101");
        resigningEmployee.setOrganization(testOrg);
        resigningEmployee.setStatus("ACTIVE");
        resigningEmployee.setManager(managerEmployee);
        resigningEmployee.setDepartment("Engineering");

        itOfficer = new Employee();
        itOfficer.setId(301L);
        itOfficer.setFullName("Bob IT");
        itOfficer.setEmail("bob.it@acme.com");
        itOfficer.setEmployeeId("EMP-301");
        itOfficer.setOrganization(testOrg);
        itOfficer.setStatus("ACTIVE");
        itOfficer.setDepartment("IT");

        adminOfficer = new Employee();
        adminOfficer.setId(302L);
        adminOfficer.setFullName("Carol Admin");
        adminOfficer.setEmail("carol.admin@acme.com");
        adminOfficer.setEmployeeId("EMP-302");
        adminOfficer.setOrganization(testOrg);
        adminOfficer.setStatus("ACTIVE");
        adminOfficer.setDepartment("Administration");

        financeOfficer = new Employee();
        financeOfficer.setId(303L);
        financeOfficer.setFullName("David Finance");
        financeOfficer.setEmail("david.fin@acme.com");
        financeOfficer.setEmployeeId("EMP-303");
        financeOfficer.setOrganization(testOrg);
        financeOfficer.setStatus("ACTIVE");
        financeOfficer.setDepartment("Finance");

        hrUser = new User();
        hrUser.setId(10L);
        hrUser.setWorkEmail("hr@acme.com");
        hrUser.setFullName("HR Admin");
        hrUser.setOrganization(testOrg);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Exit Request Creation triggers manager approval and sets status to MANAGER_APPROVAL_PENDING")
    void testCreateExitRequest_Success() {
        CreateExitRequest req = new CreateExitRequest();
        req.setEmployeeId(101L);
        req.setExitType("RESIGNATION");
        req.setResignationDate(LocalDate.now());
        req.setRequestedLastWorkingDate(LocalDate.now().plusDays(30));
        req.setReason("Pursuing higher studies");
        req.setRemarks("Standard resignation request");

        when(employeeRepository.findById(101L)).thenReturn(Optional.of(resigningEmployee));
        when(exitRepository.findActiveExitsForEmployee(10L, 101L)).thenReturn(Collections.emptyList());
        when(exitRepository.save(any(EmployeeExit.class))).thenAnswer(invocation -> {
            EmployeeExit e = invocation.getArgument(0);
            e.setId(501L);
            return e;
        });

        ExitResponse response = exitService.createExitRequest(hrUser, req);

        assertNotNull(response);
        assertEquals(501L, response.getExitId());
        assertEquals("MANAGER_APPROVAL_PENDING", response.getStatus());
        assertEquals(LocalDate.now().plusDays(30), response.getRequestedLastWorkingDate());

        // Verify manager approval workflow was initiated
        verify(approvalWorkflowEngineService, times(1)).startWorkflow(
                eq(WorkflowType.EMPLOYEE_EXIT),
                eq("EMPLOYEE_EXIT"),
                eq("501"),
                eq(resigningEmployee),
                isNull()
        );
    }

    @Test
    @DisplayName("Manager Approval event transitions Exit status to HR_OFFBOARDING_PENDING")
    void testManagerApproval_TransitionsExitStatus() {
        EmployeeExit exit = new EmployeeExit();
        exit.setId(501L);
        exit.setOrganization(testOrg);
        exit.setEmployee(resigningEmployee);
        exit.setStatus("MANAGER_APPROVAL_PENDING");

        when(exitRepository.findById(501L)).thenReturn(Optional.of(exit));

        ApprovalWorkflowCompletedEvent completedEvent = new ApprovalWorkflowCompletedEvent(
                this,
                "INST-EXIT-501",
                WorkflowType.EMPLOYEE_EXIT,
                "EMPLOYEE_EXIT",
                "501",
                10L,
                ApprovalStatus.APPROVED
        );

        eventListener.onApprovalWorkflowCompleted(completedEvent);

        verify(exitRepository, times(1)).save(argThat(e -> "HR_OFFBOARDING_PENDING".equals(e.getStatus())));
    }

    @Test
    @DisplayName("Manager Rejection event transitions Exit status to REJECTED")
    void testManagerRejection_TransitionsExitStatus() {
        EmployeeExit exit = new EmployeeExit();
        exit.setId(501L);
        exit.setOrganization(testOrg);
        exit.setEmployee(resigningEmployee);
        exit.setStatus("MANAGER_APPROVAL_PENDING");

        when(exitRepository.findById(501L)).thenReturn(Optional.of(exit));

        ApprovalWorkflowRejectedEvent rejectedEvent = new ApprovalWorkflowRejectedEvent(
                this,
                "INST-EXIT-501",
                WorkflowType.EMPLOYEE_EXIT,
                "EMPLOYEE_EXIT",
                "501",
                10L,
                "Notice period waiver denied"
        );

        eventListener.onApprovalWorkflowRejected(rejectedEvent);

        verify(exitRepository, times(1)).save(argThat(e -> "REJECTED".equals(e.getStatus())));
    }

    @Test
    @DisplayName("HR Offboarding creates 4 clearance records (IT, ADMIN, FINANCE, MANAGER) with assignees and instructions")
    void testInitiateOffboarding_CreatesClearances() {
        EmployeeExit exit = new EmployeeExit();
        exit.setId(501L);
        exit.setOrganization(testOrg);
        exit.setEmployee(resigningEmployee);
        exit.setReportingManager(managerEmployee);
        exit.setStatus("HR_OFFBOARDING_PENDING");
        exit.setRequestedLastWorkingDate(LocalDate.now().plusDays(30));

        when(exitRepository.findByIdAndOrganizationId(501L, 10L)).thenReturn(Optional.of(exit));
        when(exitRepository.save(any(EmployeeExit.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(employeeRepository.findByOrganizationIdAndDepartment(10L, "IT")).thenReturn(List.of(itOfficer));
        when(employeeRepository.findByOrganizationIdAndDepartment(10L, "Administration")).thenReturn(List.of(adminOfficer));
        when(employeeRepository.findByOrganizationIdAndDepartment(10L, "Finance")).thenReturn(List.of(financeOfficer));
        when(clearanceRepository.findByExitIdAndDepartment(eq(501L), anyString())).thenReturn(Optional.empty());
        when(clearanceRepository.save(any(ExitClearance.class))).thenAnswer(invocation -> {
            ExitClearance c = invocation.getArgument(0);
            c.setId((long) (Math.random() * 1000 + 1));
            return c;
        });

        // Test clearance service directly through exitService
        ReflectionTestUtils.setField(exitService, "exitClearanceService", clearanceService);
        ReflectionTestUtils.setField(clearanceService, "clearanceRepository", clearanceRepository);
        ReflectionTestUtils.setField(clearanceService, "employeeRepository", employeeRepository);
        ReflectionTestUtils.setField(clearanceService, "objectMapper", objectMapper);

        HrOffboardingRequest hrReq = new HrOffboardingRequest();
        hrReq.setLastWorkingDate(LocalDate.now().plusDays(25));
        hrReq.setNoticePeriodDays(30);
        hrReq.setNoticeServedDays(25);
        hrReq.setRemarks("Initiating department clearances");

        OffboardingInitiationResponse resp = exitService.initiateOffboarding(hrUser, 501L, hrReq);

        assertNotNull(resp);
        assertEquals(501L, resp.getExitId());
        assertEquals("CLEARANCE_PENDING", resp.getStatus());
        assertEquals(4, resp.getClearances().size());
        assertEquals(LocalDate.now().plusDays(25), resp.getLastWorkingDate());

        // Verify 4 distinct clearances saved
        verify(clearanceRepository, times(4)).save(any(ExitClearance.class));
    }

    @Test
    @DisplayName("Update Clearance Assignment validates tenant boundary, active status, and updates reason/instructions")
    void testUpdateClearanceAssignment_Success() {
        EmployeeExit exit = new EmployeeExit();
        exit.setId(501L);
        exit.setOrganization(testOrg);

        ExitClearance clearance = new ExitClearance();
        clearance.setId(701L);
        clearance.setExit(exit);
        clearance.setOrganization(testOrg);
        clearance.setDepartment("IT");
        clearance.setAssignedTo(itOfficer);
        clearance.setClearanceReason("Default verify assets");
        clearance.setStatus("PENDING");

        Employee newItOfficer = new Employee();
        newItOfficer.setId(399L);
        newItOfficer.setFullName("Sara Senior IT");
        newItOfficer.setOrganization(testOrg);
        newItOfficer.setStatus("ACTIVE");

        when(clearanceRepository.findByIdAndOrganizationId(701L, 10L)).thenReturn(Optional.of(clearance));
        when(employeeRepository.findById(399L)).thenReturn(Optional.of(newItOfficer));
        when(clearanceRepository.save(any(ExitClearance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClearanceAssignmentRequest assignReq = new ClearanceAssignmentRequest();
        assignReq.setAssignedToUserId(399L);
        assignReq.setClearanceReason("Verify Mac M3 return, charger, and revoke GitHub access");
        assignReq.setRemarks("High priority offboarding");

        ExitClearanceDto result = clearanceService.updateClearanceAssignment(hrUser, 501L, 701L, assignReq);

        assertNotNull(result);
        assertEquals(399L, result.getAssignedToId());
        assertEquals("Sara Senior IT", result.getAssignedToName());
        assertEquals("Verify Mac M3 return, charger, and revoke GitHub access", result.getClearanceReason());
        assertEquals("High priority offboarding", result.getRemarks());
    }

    @Test
    @DisplayName("Update Clearance Assignment throws exception for cross-tenant assignee")
    void testUpdateClearanceAssignment_CrossTenantFails() {
        EmployeeExit exit = new EmployeeExit();
        exit.setId(501L);
        exit.setOrganization(testOrg);

        ExitClearance clearance = new ExitClearance();
        clearance.setId(701L);
        clearance.setExit(exit);
        clearance.setOrganization(testOrg);

        Organization otherOrg = new Organization();
        otherOrg.setId(999L);

        Employee foreignEmployee = new Employee();
        foreignEmployee.setId(888L);
        foreignEmployee.setOrganization(otherOrg);
        foreignEmployee.setStatus("ACTIVE");

        when(clearanceRepository.findByIdAndOrganizationId(701L, 10L)).thenReturn(Optional.of(clearance));
        when(employeeRepository.findById(888L)).thenReturn(Optional.of(foreignEmployee));

        ClearanceAssignmentRequest assignReq = new ClearanceAssignmentRequest();
        assignReq.setAssignedToUserId(888L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                clearanceService.updateClearanceAssignment(hrUser, 501L, 701L, assignReq)
        );
        assertTrue(ex.getMessage().contains("does not belong to the organization"));
    }

    @Test
    @DisplayName("Clearance Action HOLD sets status to HOLD and stores checklist data")
    void testClearanceAction_Hold() {
        EmployeeExit exit = new EmployeeExit();
        exit.setId(501L);
        exit.setOrganization(testOrg);

        ExitClearance clearance = new ExitClearance();
        clearance.setId(701L);
        clearance.setExit(exit);
        clearance.setOrganization(testOrg);
        clearance.setDepartment("IT");
        clearance.setAssignedTo(itOfficer);
        clearance.setStatus("PENDING");

        when(clearanceRepository.findByIdAndOrganizationId(701L, 10L)).thenReturn(Optional.of(clearance));
        when(employeeRepository.findByEmail("hr@acme.com")).thenReturn(Optional.of(itOfficer));
        when(clearanceRepository.findByExitId(501L)).thenReturn(List.of(clearance));
        when(clearanceRepository.save(any(ExitClearance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClearanceActionRequest actionReq = new ClearanceActionRequest();
        actionReq.setAction("HOLD");
        actionReq.setRemarks("Laptop charger pending return");
        actionReq.setClearanceData(Map.of("laptopReturned", true, "chargerReturned", false));

        ExitClearanceDto dto = clearanceService.processClearanceAction(hrUser, 501L, 701L, actionReq);

        assertNotNull(dto);
        assertEquals("HOLD", dto.getStatus());
        assertEquals("Laptop charger pending return", dto.getRemarks());
        assertNotNull(dto.getClearanceData());
        verify(exitRepository, atLeastOnce()).save(argThat(e -> "CLEARANCE_PENDING".equals(e.getStatus())));
    }

    @Test
    @DisplayName("Clearance Action CLEAR records audit separation: assigned_to_id != cleared_by_id")
    void testClearanceAction_Clear_AuditSeparation() {
        EmployeeExit exit = new EmployeeExit();
        exit.setId(501L);
        exit.setOrganization(testOrg);

        ExitClearance clearance = new ExitClearance();
        clearance.setId(701L);
        clearance.setExit(exit);
        clearance.setOrganization(testOrg);
        clearance.setDepartment("IT");
        clearance.setAssignedTo(itOfficer); // Assigned to Bob IT (ID 301)
        clearance.setStatus("PENDING");

        // Cleared by Carol Admin / HR actor (ID 302)
        when(clearanceRepository.findByIdAndOrganizationId(701L, 10L)).thenReturn(Optional.of(clearance));
        when(employeeRepository.findByEmail("hr@acme.com")).thenReturn(Optional.of(adminOfficer));
        when(clearanceRepository.findByExitId(501L)).thenReturn(List.of(clearance));
        when(clearanceRepository.save(any(ExitClearance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClearanceActionRequest actionReq = new ClearanceActionRequest();
        actionReq.setAction("CLEAR");
        actionReq.setRemarks("Verified asset handover and revoked active directory permissions");
        actionReq.setClearanceData(Map.of("laptopReturned", true, "accessRevoked", true));

        ExitClearanceDto dto = clearanceService.processClearanceAction(hrUser, 501L, 701L, actionReq);

        assertNotNull(dto);
        assertEquals("CLEARED", dto.getStatus());
        assertEquals(301L, dto.getAssignedToId()); // assigned to Bob
        assertEquals(302L, dto.getClearedById());   // cleared by Carol
        assertNotNull(dto.getClearedAt());
        assertNotEquals(dto.getAssignedToId(), dto.getClearedById(), "Audit trail must capture assigner/assignee vs clearing actor");
    }

    @Test
    @DisplayName("When all 4 clearances are CLEARED, Exit status transitions to FNF_CALCULATION_PENDING")
    void testAllClearancesCleared_AdvancesToFnfPending() {
        EmployeeExit exit = new EmployeeExit();
        exit.setId(501L);
        exit.setOrganization(testOrg);
        exit.setStatus("CLEARANCE_IN_PROGRESS");

        ExitClearance c1 = createMockClearance(1L, exit, "IT", "CLEARED");
        ExitClearance c2 = createMockClearance(2L, exit, "ADMIN", "CLEARED");
        ExitClearance c3 = createMockClearance(3L, exit, "FINANCE", "CLEARED");
        ExitClearance c4 = createMockClearance(4L, exit, "MANAGER", "PENDING");

        when(clearanceRepository.findByIdAndOrganizationId(4L, 10L)).thenReturn(Optional.of(c4));
        when(employeeRepository.findByEmail("hr@acme.com")).thenReturn(Optional.of(managerEmployee));
        when(clearanceRepository.save(any(ExitClearance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When c4 is cleared, findByExitId will return all 4 CLEARED
        when(clearanceRepository.findByExitId(501L)).thenAnswer(inv -> {
            c4.setStatus("CLEARED");
            return List.of(c1, c2, c3, c4);
        });

        ClearanceActionRequest actionReq = new ClearanceActionRequest();
        actionReq.setAction("CLEAR");
        actionReq.setRemarks("Knowledge transfer fully completed");

        ExitClearanceDto dto = clearanceService.processClearanceAction(hrUser, 501L, 4L, actionReq);

        assertNotNull(dto);
        assertEquals("CLEARED", dto.getStatus());
        verify(exitRepository, times(1)).save(argThat(e -> "FNF_CALCULATION_PENDING".equals(e.getStatus())));
    }

    private ExitClearance createMockClearance(Long id, EmployeeExit exit, String dept, String status) {
        ExitClearance c = new ExitClearance();
        c.setId(id);
        c.setExit(exit);
        c.setOrganization(testOrg);
        c.setDepartment(dept);
        c.setStatus(status);
        c.setAssignedTo(itOfficer);
        c.setClearanceReason("Verify " + dept);
        return c;
    }
}
