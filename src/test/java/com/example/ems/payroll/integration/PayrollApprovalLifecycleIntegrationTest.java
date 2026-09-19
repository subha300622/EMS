package com.example.ems.payroll.integration;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalChangesRequestedEvent;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.approval.event.ApprovalWorkflowRejectedEvent;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.payroll.dto.PayrollRunCreateRequest;
import com.example.ems.payroll.dto.PayrollRunResponse;
import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.event.PayrollApprovalEventListener;
import com.example.ems.payroll.repository.*;
import com.example.ems.payroll.service.PayrollRunService;
import com.example.ems.security.context.TenantContext;
import com.example.ems.config.GlobalExceptionHandler;
import com.example.ems.payroll.controller.PayrollEmployeeController;
import com.example.ems.payroll.controller.PayrollRunController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PayrollApprovalLifecycleIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private PayrollEmployeeController payrollEmployeeController;

    @Autowired
    private PayrollRunController payrollRunController;

    @Autowired
    private PayrollRunService payrollRunService;

    @Autowired
    private PayrollApprovalEventListener approvalEventListener;

    @Autowired
    private PayrollRunRepository payrollRunRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private SalaryStructureRepository salaryStructureRepository;

    @Autowired
    private SalaryComponentRepository salaryComponentRepository;

    @Autowired
    private SalaryStructureComponentRepository salaryStructureComponentRepository;

    @Autowired
    private EmployeeSalaryAssignmentRepository employeeSalaryAssignmentRepository;

    private Organization organization;
    private Employee employee;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(payrollRunController, payrollEmployeeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        long ts = System.currentTimeMillis();
        organization = new Organization();
        organization.setName("Lifecycle Test Org " + ts);
        organization.setOrganizationCode("ORG_LC_" + ts);
        organization = organizationRepository.save(organization);

        TenantContext.setCurrentTenant(organization.getId());

        Department department = new Department();
        department.setName("Finance " + ts);
        department.setCode("FIN_" + ts);
        department.setOrganization(organization);
        department = departmentRepository.save(department);

        employee = new Employee();
        employee.setEmployeeId("EMP_LC_" + ts);
        employee.setFullName("Alice Lifecycle");
        employee.setEmail("alice_" + ts + "@lifecycle.com");
        employee.setDepartment(department.getName());
        employee.setStatus("ACTIVE");
        employee.setOrganization(organization);
        employee = employeeRepository.save(employee);

        SalaryComponent basicComp = salaryComponentRepository.save(
                new SalaryComponent(organization.getId(), "Basic", "BASIC", "Basic Salary", SalaryComponentType.EARNING, true, true)
        );
        SalaryComponent hraComp = salaryComponentRepository.save(
                new SalaryComponent(organization.getId(), "HRA", "HRA", "House Rent Allowance", SalaryComponentType.EARNING, true, true)
        );

        SalaryStructure structure = new SalaryStructure();
        structure.setOrganizationId(organization.getId());
        structure.setName("Finance Structure " + ts);
        structure.setCode("FIN_STR_" + ts);
        structure.setStatus(SalaryStructureStatus.ACTIVE);
        structure.setVersion(1);
        structure = salaryStructureRepository.save(structure);

        salaryStructureComponentRepository.save(new SalaryStructureComponent(
                structure, basicComp, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(60000), null, null, 1
        ));
        salaryStructureComponentRepository.save(new SalaryStructureComponent(
                structure, hraComp, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(20000), null, null, 2
        ));

        employeeSalaryAssignmentRepository.save(new EmployeeSalaryAssignment(
                organization.getId(), employee, structure, LocalDate.of(2026, 1, 1), null, SalaryAssignmentStatus.ACTIVE, "Initial"
        ));
    }

    @AfterEach
    public void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("End-to-End Approval Lifecycle: Draft -> Calculated -> Drilldowns -> Pending Approval -> Approved -> Locked -> Finalized")
    public void testFullPayrollApprovalLifecycle() throws Exception {
        LocalDate periodStart = LocalDate.of(2026, 9, 1);
        LocalDate periodEnd = LocalDate.of(2026, 9, 30);

        // 1. Create Run (DRAFT)
        PayrollRunCreateRequest createReq = new PayrollRunCreateRequest(periodStart, periodEnd, "INR");
        PayrollRunResponse created = payrollRunService.createPayrollRun(createReq);
        assertEquals(PayrollRunStatus.DRAFT, created.getStatus());

        // 2. Process Run (CALCULATED)
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/process", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CALCULATED"))
                .andExpect(jsonPath("$.data.totalGross").value(80000.0))
                .andExpect(jsonPath("$.data.totalNet").value(80000.0));

        // 3. Drill-Down: Show Employees
        mockMvc.perform(get("/api/v1/payroll/runs/{id}/employees", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].employeeCode").value(employee.getEmployeeId()))
                .andExpect(jsonPath("$.data[0].grossAmount").value(80000.0))
                .andExpect(jsonPath("$.data[0].netAmount").value(80000.0));

        // 4. Drill-Down: Show Payroll Items for Employee
        mockMvc.perform(get("/api/v1/payroll/runs/{id}/employees/{peId}/items", created.getId(), employee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        // 5. Drill-Down: Show Employee Payslip
        mockMvc.perform(get("/api/v1/payroll/runs/{id}/employees/{peId}/payslip", created.getId(), employee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.employeeName").value("Alice Lifecycle"))
                .andExpect(jsonPath("$.data.grossAmount").value(80000.0))
                .andExpect(jsonPath("$.data.netAmount").value(80000.0))
                .andExpect(jsonPath("$.data.items").isArray());

        // 6. Submit for Approval (PENDING_APPROVAL)
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/submit-approval", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING_APPROVAL"))
                .andExpect(jsonPath("$.data.approvalInstanceId").isNotEmpty());

        PayrollRun pendingRun = payrollRunRepository.findById(created.getId()).orElseThrow();
        assertEquals(PayrollRunStatus.PENDING_APPROVAL, pendingRun.getStatus());
        String approvalInstanceId = pendingRun.getApprovalInstanceId();
        assertNotNull(approvalInstanceId);

        // 7. Simulate Approval Workflow Completion Event
        ApprovalWorkflowCompletedEvent completedEvent = new ApprovalWorkflowCompletedEvent(
                this, approvalInstanceId, WorkflowType.PAYROLL_APPROVAL, "PAYROLL", created.getId().toString(), organization.getId(), ApprovalStatus.APPROVED
        );
        approvalEventListener.onApprovalWorkflowCompleted(completedEvent);

        PayrollRun approvedRun = payrollRunRepository.findById(created.getId()).orElseThrow();
        assertEquals(PayrollRunStatus.APPROVED, approvedRun.getStatus());
        assertNotNull(approvedRun.getApprovedAt());

        // 8. Lock Payroll Run (LOCKED)
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/lock", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("LOCKED"))
                .andExpect(jsonPath("$.data.finalizedAt").isNotEmpty());

        PayrollRun lockedRun = payrollRunRepository.findById(created.getId()).orElseThrow();
        assertEquals(PayrollRunStatus.LOCKED, lockedRun.getStatus());

        // 9. Finalize Payroll Run (FINALIZED)
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/finalize", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("FINALIZED"));

        PayrollRun finalizedRun = payrollRunRepository.findById(created.getId()).orElseThrow();
        assertEquals(PayrollRunStatus.FINALIZED, finalizedRun.getStatus());
    }

    @Test
    @DisplayName("Approval Cancellation Flow: Pending Approval -> Cancel -> Back to CALCULATED")
    public void testApprovalCancellationFlow() throws Exception {
        LocalDate periodStart = LocalDate.of(2026, 10, 1);
        LocalDate periodEnd = LocalDate.of(2026, 10, 31);

        PayrollRunResponse created = payrollRunService.createPayrollRun(new PayrollRunCreateRequest(periodStart, periodEnd, "INR"));
        payrollRunService.processPayrollRun(created.getId());
        payrollRunService.submitForApproval(created.getId());

        // Cancel approval
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/cancel-approval", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Need to adjust sales incentives\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CALCULATED"));

        PayrollRun run = payrollRunRepository.findById(created.getId()).orElseThrow();
        assertEquals(PayrollRunStatus.CALCULATED, run.getStatus());

        // Re-process is allowed in CALCULATED status
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/process", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CALCULATED"));
    }

    @Test
    @DisplayName("Rejection Flow: Pending Approval -> Rejected Event -> Recalculate -> Resubmit")
    public void testApprovalRejectionAndRecalculationFlow() throws Exception {
        LocalDate periodStart = LocalDate.of(2026, 11, 1);
        LocalDate periodEnd = LocalDate.of(2026, 11, 30);

        PayrollRunResponse created = payrollRunService.createPayrollRun(new PayrollRunCreateRequest(periodStart, periodEnd, "INR"));
        payrollRunService.processPayrollRun(created.getId());
        PayrollRunResponse submitted = payrollRunService.submitForApproval(created.getId());

        // Simulate rejection event
        ApprovalWorkflowRejectedEvent rejectedEvent = new ApprovalWorkflowRejectedEvent(
                this, submitted.getApprovalInstanceId(), WorkflowType.PAYROLL_APPROVAL, "PAYROLL", created.getId().toString(), organization.getId(), "Bonus threshold exceeded"
        );
        approvalEventListener.onApprovalWorkflowRejected(rejectedEvent);

        PayrollRun rejectedRun = payrollRunRepository.findById(created.getId()).orElseThrow();
        assertEquals(PayrollRunStatus.REJECTED, rejectedRun.getStatus());
        assertEquals("Bonus threshold exceeded", rejectedRun.getRejectionReason());

        // Recalculate from REJECTED state is allowed
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/process", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CALCULATED"));

        // Resubmit for approval
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/submit-approval", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING_APPROVAL"));
    }

    @Test
    @DisplayName("Strict Immutability Guards: Re-calculating in Pending, Approved, or Locked state returns 409 Conflict")
    public void testStrictImmutabilityGuards() throws Exception {
        LocalDate periodStart = LocalDate.of(2026, 12, 1);
        LocalDate periodEnd = LocalDate.of(2026, 12, 31);

        PayrollRunResponse created = payrollRunService.createPayrollRun(new PayrollRunCreateRequest(periodStart, periodEnd, "INR"));
        payrollRunService.processPayrollRun(created.getId());
        PayrollRunResponse submitted = payrollRunService.submitForApproval(created.getId());

        // 1. Guard against re-calculation in PENDING_APPROVAL
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/process", created.getId()))
                .andExpect(status().isConflict());

        // 2. Guard against re-calculation in APPROVED
        ApprovalWorkflowCompletedEvent completedEvent = new ApprovalWorkflowCompletedEvent(
                this, submitted.getApprovalInstanceId(), WorkflowType.PAYROLL_APPROVAL, "PAYROLL", created.getId().toString(), organization.getId(), ApprovalStatus.APPROVED
        );
        approvalEventListener.onApprovalWorkflowCompleted(completedEvent);

        mockMvc.perform(post("/api/v1/payroll/runs/{id}/process", created.getId()))
                .andExpect(status().isConflict());

        // 3. Guard against re-calculation in LOCKED
        payrollRunService.lockPayrollRun(created.getId());
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/process", created.getId()))
                .andExpect(status().isConflict());

        // 4. Guard against re-calculation in FINALIZED
        payrollRunService.finalizePayrollRun(created.getId());
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/process", created.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Changes Requested Flow: Pending Approval -> Changes Requested Event -> Recalculate -> Resubmit")
    public void testApprovalChangesRequestedAndRecalculationFlow() throws Exception {
        LocalDate periodStart = LocalDate.of(2027, 2, 1);
        LocalDate periodEnd = LocalDate.of(2027, 2, 28);

        PayrollRunResponse created = payrollRunService.createPayrollRun(new PayrollRunCreateRequest(periodStart, periodEnd, "INR"));
        payrollRunService.processPayrollRun(created.getId());
        PayrollRunResponse submitted = payrollRunService.submitForApproval(created.getId());

        // Simulate changes requested event
        ApprovalChangesRequestedEvent changesEvent = new ApprovalChangesRequestedEvent(
                this, submitted.getApprovalInstanceId(), WorkflowType.PAYROLL_APPROVAL, "PAYROLL", created.getId().toString(), organization.getId(), "Please review LOP calculations"
        );
        approvalEventListener.onApprovalChangesRequested(changesEvent);

        PayrollRun changesRun = payrollRunRepository.findById(created.getId()).orElseThrow();
        assertEquals(PayrollRunStatus.CHANGES_REQUESTED, changesRun.getStatus());
        assertEquals("Please review LOP calculations", changesRun.getRejectionReason());

        // Recalculate from CHANGES_REQUESTED state is allowed
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/process", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CALCULATED"));

        // Resubmit for approval
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/submit-approval", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING_APPROVAL"));
    }

    @Test
    @DisplayName("Invalid State Guard: Cannot submit uncalculated DRAFT or lock unapproved run")
    public void testInvalidStateTransitions() throws Exception {
        LocalDate periodStart = LocalDate.of(2027, 1, 1);
        LocalDate periodEnd = LocalDate.of(2027, 1, 31);

        PayrollRunResponse created = payrollRunService.createPayrollRun(new PayrollRunCreateRequest(periodStart, periodEnd, "INR"));

        // Cannot submit DRAFT for approval
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/submit-approval", created.getId()))
                .andExpect(status().isConflict());

        // Cannot lock unapproved run
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/lock", created.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Tenant Isolation: A tenant cannot access, process, submit, lock, or finalize another tenant's payroll run")
    public void testTenantIsolation() throws Exception {
        LocalDate periodStart = LocalDate.of(2027, 3, 1);
        LocalDate periodEnd = LocalDate.of(2027, 3, 31);

        PayrollRunResponse orgARun = payrollRunService.createPayrollRun(new PayrollRunCreateRequest(periodStart, periodEnd, "INR"));
        payrollRunService.processPayrollRun(orgARun.getId());

        // Create Tenant B
        long ts = System.currentTimeMillis();
        Organization orgB = new Organization();
        orgB.setName("Tenant B " + ts);
        orgB.setOrganizationCode("ORG_B_" + ts);
        orgB = organizationRepository.save(orgB);

        // Switch to Tenant B context
        TenantContext.setCurrentTenant(orgB.getId());

        // 1. Cannot get Tenant A's run
        mockMvc.perform(get("/api/v1/payroll/runs/{id}", orgARun.getId()))
                .andExpect(status().isNotFound());

        // 2. Cannot list employees for Tenant A's run
        mockMvc.perform(get("/api/v1/payroll/runs/{id}/employees", orgARun.getId()))
                .andExpect(status().isNotFound());

        // 3. Cannot process Tenant A's run
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/process", orgARun.getId()))
                .andExpect(status().isNotFound());

        // 4. Cannot submit Tenant A's run for approval
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/submit-approval", orgARun.getId()))
                .andExpect(status().isNotFound());

        // 5. Cannot lock Tenant A's run
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/lock", orgARun.getId()))
                .andExpect(status().isNotFound());

        // 6. Cannot finalize Tenant A's run
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/finalize", orgARun.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/payroll/runs/{runId}/payslips - Successfully retrieve all employee payslips for a run")
    public void testGetPayrollRunPayslips() throws Exception {
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 6, 30);
        PayrollRunCreateRequest createReq = new PayrollRunCreateRequest(start, end, "INR");
        PayrollRunResponse run = payrollRunService.createPayrollRun(createReq);

        // Process run to generate employee snapshots and items
        mockMvc.perform(post("/api/v1/payroll/runs/{id}/process", run.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CALCULATED"));

        // Fetch payslips for the run
        mockMvc.perform(get("/api/v1/payroll/runs/{id}/payslips", run.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].payrollRunId").value(run.getId()))
                .andExpect(jsonPath("$.data[0].employeeId").value(employee.getId()))
                .andExpect(jsonPath("$.data[0].grossAmount").value(80000.00))
                .andExpect(jsonPath("$.data[0].netAmount").value(80000.00))
                .andExpect(jsonPath("$.data[0].items").isArray());
    }
}