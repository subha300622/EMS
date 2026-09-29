package com.example.ems.overtime.integration;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceStatus;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.overtime.dto.OvertimeAdjustmentRequest;
import com.example.ems.overtime.dto.OvertimePreviewResponse;
import com.example.ems.overtime.dto.OvertimeRecordResponse;
import com.example.ems.overtime.entity.*;
import com.example.ems.overtime.listener.OvertimeApprovalEventListener;
import com.example.ems.overtime.repository.OvertimePolicyRepository;
import com.example.ems.overtime.repository.OvertimeRecordRepository;
import com.example.ems.overtime.service.OvertimeAdjustmentService;
import com.example.ems.overtime.service.OvertimeCalculationService;
import com.example.ems.overtime.service.OvertimePayrollIntegrationService;
import com.example.ems.overtime.service.OvertimeWorkflowService;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class OvertimeFlowIntegrationTest {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private OvertimePolicyRepository policyRepository;

    @Autowired
    private OvertimeRecordRepository recordRepository;

    @Autowired
    private OvertimeCalculationService calculationService;

    @Autowired
    private OvertimeAdjustmentService adjustmentService;

    @Autowired
    private OvertimeWorkflowService workflowService;

    @Autowired
    private OvertimeApprovalEventListener approvalEventListener;

    @Autowired
    private OvertimePayrollIntegrationService payrollIntegrationService;

    private Organization org1;
    private Organization org2;
    private Employee emp1;
    private Employee emp2;
    private Attendance att1;
    private OvertimePolicy policy1;

    @BeforeEach
    void setUp() {
        // 1. Setup Tenant A
        long now = System.currentTimeMillis();
        org1 = new Organization();
        org1.setName("Tenant A Corp");
        org1.setOrganizationCode("ORG-A-" + now);
        org1.setEmail("admin" + now + "@tenant-a.com");
        org1 = organizationRepository.save(org1);

        // 2. Setup Tenant B
        org2 = new Organization();
        org2.setName("Tenant B Corp");
        org2.setOrganizationCode("ORG-B-" + now);
        org2.setEmail("admin" + now + "@tenant-b.com");
        org2 = organizationRepository.save(org2);

        TenantContext.setCurrentTenant(org1.getId());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("hr@tenant-a.com", "password")
        );

        // Employee in Org 1
        emp1 = new Employee();
        emp1.setFullName("Alice Smith");
        emp1.setEmail("alice.smith@tenant-a.com");
        emp1.setEmployeeId("EMP-A-001");
        emp1.setAnnualSalary(BigDecimal.valueOf(624000)); // 26,000 basic
        emp1.setOrganization(org1);
        emp1 = employeeRepository.save(emp1);

        // Employee in Org 2
        emp2 = new Employee();
        emp2.setFullName("Bob Jones");
        emp2.setEmail("bob.jones@tenant-b.com");
        emp2.setEmployeeId("EMP-B-001");
        emp2.setOrganization(org2);
        emp2 = employeeRepository.save(emp2);

        // Active OT Policy for Org 1
        policy1 = new OvertimePolicy();
        policy1.setOrganization(org1);
        policy1.setName("Org 1 Default Policy");
        policy1.setStatus(OvertimePolicyStatus.ACTIVE);
        policy1.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        policy1.setNormalWorkingHours(8); // 480 mins scheduled
        policy1.setMinimumOtMinutes(30);
        policy1.setMaximumOtMinutes(240); // 4 hrs cap
        policy1.setAmountBasis(OvertimeAmountBasis.BASIC_SALARY);
        policy1.setWorkingDaysPerMonth(26);
        policy1.setWorkingHoursPerDay(8); // 208 hrs/month -> hourlyRate = 125.00
        policy1.setNormalDayMultiplier(BigDecimal.valueOf(1.50));
        policy1.setWeekendMultiplier(BigDecimal.valueOf(2.00));
        policy1.setHolidayMultiplier(BigDecimal.valueOf(2.00));
        policy1.setApprovalRequired(true);
        policy1 = policyRepository.save(policy1);

        // Attendance for Emp 1 on Wednesday (Normal day): 11 hrs presence, 1 hr break -> 10 hrs worked (2 hrs OT)
        att1 = new Attendance();
        att1.setOrganization(org1);
        att1.setEmployee(emp1);
        att1.setDate(LocalDate.of(2026, 9, 9));
        att1.setStatus(AttendanceStatus.PRESENT);
        att1.setCheckInTime(Instant.parse("2026-09-09T03:30:00Z"));
        att1.setCheckOutTime(Instant.parse("2026-09-09T14:30:00Z"));
        att1.setTotalBreakMinutes(60);
        att1.setTotalWorkingMinutes(600);
        att1 = attendanceRepository.save(att1);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void testEndToEndOvertimeLifecycle() {
        TenantContext.setCurrentTenant(org1.getId());

        // 1. Preview Calculation (Stateless)
        OvertimePreviewResponse preview = calculationService.previewOvertime(att1.getId());
        assertNotNull(preview);
        assertEquals(480, preview.getScheduledMinutes());
        assertEquals(600, preview.getWorkedMinutes());
        assertEquals(120, preview.getCalculatedOtMinutes()); // 2 hrs OT
        assertEquals(new BigDecimal("125.00"), preview.getHourlyRate());
        assertEquals(new BigDecimal("187.50"), preview.getOtRate());
        assertEquals(new BigDecimal("375.00"), preview.getCalculatedAmount());

        // 2. Persist Calculation
        OvertimeRecordResponse calculatedRecord = calculationService.calculateOvertime(att1.getId());
        assertNotNull(calculatedRecord);
        Long recordId = calculatedRecord.getId();
        assertEquals(OvertimeStatus.CALCULATED, calculatedRecord.getStatus());
        assertEquals(120, calculatedRecord.getCalculatedOtMinutes());
        assertEquals(new BigDecimal("375.00"), calculatedRecord.getCalculatedAmount());

        // 3. Manager Adjustment (Adjust down to 90 mins = 1.5 hrs)
        OvertimeAdjustmentRequest adjReq = new OvertimeAdjustmentRequest(90, "Authorized 1.5 hours only");
        OvertimeRecordResponse adjustedRecord = adjustmentService.adjustOvertime(recordId, adjReq);
        assertEquals(OvertimeStatus.ADJUSTED, adjustedRecord.getStatus());
        // Original values preserved
        assertEquals(120, adjustedRecord.getCalculatedOtMinutes());
        assertEquals(new BigDecimal("375.00"), adjustedRecord.getCalculatedAmount());
        // Adjusted values updated: 1.5 hrs * 187.50 = 281.25
        assertEquals(90, adjustedRecord.getAdjustedOtMinutes());
        assertEquals(new BigDecimal("281.25"), adjustedRecord.getAdjustedAmount());

        // 4. Submit to Central Approval Engine
        OvertimeRecordResponse submittedRecord = workflowService.submitOvertime(recordId);
        assertEquals(OvertimeStatus.PENDING_APPROVAL, submittedRecord.getStatus());

        // 5. Complete Central Approval Workflow (Simulate APPROVED event)
        ApprovalWorkflowCompletedEvent approvedEvent = new ApprovalWorkflowCompletedEvent(
                this, submittedRecord.getWorkflowInstanceId(), WorkflowType.OVERTIME_REQUEST,
                "OVERTIME_RECORD", String.valueOf(recordId), org1.getId(), ApprovalStatus.APPROVED
        );
        approvalEventListener.onApprovalWorkflowCompleted(approvedEvent);

        OvertimeRecord approvedRecord = recordRepository.findById(recordId).orElseThrow();
        assertEquals(OvertimeStatus.APPROVED, approvedRecord.getStatus());
        assertEquals(90, approvedRecord.getApprovedOtMinutes());
        assertEquals(new BigDecimal("281.25"), approvedRecord.getApprovedAmount());
        assertEquals(OvertimePayrollStatus.PENDING, approvedRecord.getPayrollStatus());

        // 6. Query Eligible Records for Payroll Run
        List<OvertimeRecordResponse> eligible = payrollIntegrationService.getEligibleOvertimeRecords(
                emp1.getId(), LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)
        );
        assertEquals(1, eligible.size());
        assertEquals(recordId, eligible.get(0).getId());

        // 7. Post to Payroll Run
        Long payrollRunId = 8899L;
        BigDecimal totalPosted = payrollIntegrationService.postOvertimeToPayrollRun(
                org1.getId(), emp1.getId(), LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), payrollRunId
        );
        assertEquals(new BigDecimal("281.25"), totalPosted);

        OvertimeRecord postedRecord = recordRepository.findById(recordId).orElseThrow();
        assertEquals(OvertimeStatus.POSTED_TO_PAYROLL, postedRecord.getStatus());
        assertEquals(OvertimePayrollStatus.POSTED, postedRecord.getPayrollStatus());
        assertEquals(payrollRunId, postedRecord.getPayrollRunId());

        // 8. Verify Idempotency - Repeated posting does NOT double count
        BigDecimal secondPosted = payrollIntegrationService.postOvertimeToPayrollRun(
                org1.getId(), emp1.getId(), LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), payrollRunId
        );
        assertEquals(BigDecimal.ZERO, secondPosted);
    }

    @Test
    void testTenantIsolation_CrossTenantAccessForbidden() {
        // Calculate in Org 1
        TenantContext.setCurrentTenant(org1.getId());
        OvertimeRecordResponse record = calculationService.calculateOvertime(att1.getId());

        // Switch to Org 2 (Tenant B)
        TenantContext.setCurrentTenant(org2.getId());

        // Org 2 cannot adjust Org 1 overtime record
        assertThrows(RuntimeException.class, () ->
                adjustmentService.adjustOvertime(record.getId(), new OvertimeAdjustmentRequest(60, "Hack attempt"))
        );

        // Org 2 cannot submit Org 1 overtime record
        assertThrows(RuntimeException.class, () ->
                workflowService.submitOvertime(record.getId())
        );

        // Org 2 cannot post Org 1 overtime record
        BigDecimal posted = payrollIntegrationService.postOvertimeToPayrollRun(
                org2.getId(), emp1.getId(), LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), 9999L
        );
        assertEquals(BigDecimal.ZERO, posted);
    }
}
