package com.example.ems.incentive.integration;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalWorkflowCompletedEvent;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.incentive.dto.IncentiveAdjustmentRequest;
import com.example.ems.incentive.dto.IncentiveCalculateRequest;
import com.example.ems.incentive.dto.IncentivePreviewResponse;
import com.example.ems.incentive.dto.IncentiveRecordResponse;
import com.example.ems.incentive.dto.IncentiveSlabTierDto;
import com.example.ems.incentive.entity.*;
import com.example.ems.incentive.listener.IncentiveApprovalEventListener;
import com.example.ems.incentive.repository.IncentivePolicyRepository;
import com.example.ems.incentive.repository.IncentiveRecordRepository;
import com.example.ems.incentive.service.IncentiveAdjustmentService;
import com.example.ems.incentive.service.IncentiveCalculationService;
import com.example.ems.incentive.service.IncentivePayrollIntegrationService;
import com.example.ems.incentive.service.IncentiveWorkflowService;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
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
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class IncentiveFlowIntegrationTest {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private IncentivePolicyRepository policyRepository;

    @Autowired
    private IncentiveRecordRepository recordRepository;

    @Autowired
    private IncentiveCalculationService calculationService;

    @Autowired
    private IncentiveAdjustmentService adjustmentService;

    @Autowired
    private IncentiveWorkflowService workflowService;

    @Autowired
    private IncentiveApprovalEventListener approvalEventListener;

    @Autowired
    private IncentivePayrollIntegrationService payrollIntegrationService;

    private Organization org1;
    private Organization org2;
    private Employee emp1;
    private Employee emp2;
    private IncentivePolicy policy1;

    private final LocalDate periodStart = LocalDate.of(2026, 3, 1);
    private final LocalDate periodEnd = LocalDate.of(2026, 3, 31);

    @BeforeEach
    void setUp() {
        long now = System.currentTimeMillis();
        org1 = new Organization();
        org1.setName("Incentive Org A");
        org1.setOrganizationCode("ORG-INC-A-" + now);
        org1.setEmail("admin" + now + "@inc-a.com");
        org1 = organizationRepository.save(org1);

        org2 = new Organization();
        org2.setName("Incentive Org B");
        org2.setOrganizationCode("ORG-INC-B-" + now);
        org2.setEmail("admin" + now + "@inc-b.com");
        org2 = organizationRepository.save(org2);

        TenantContext.setCurrentTenant(org1.getId());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("hr@inc-a.com", "password")
        );

        emp1 = new Employee();
        emp1.setFullName("David Miller");
        emp1.setEmail("david.miller@inc-a.com");
        emp1.setEmployeeId("EMP-INC-001");
        emp1.setStatus("ACTIVE");
        emp1.setAnnualSalary(BigDecimal.valueOf(600000)); // 50,000 monthly
        emp1.setOrganization(org1);
        emp1 = employeeRepository.save(emp1);

        emp2 = new Employee();
        emp2.setFullName("Eva Green");
        emp2.setEmail("eva.green@inc-b.com");
        emp2.setEmployeeId("EMP-INC-002");
        emp2.setStatus("ACTIVE");
        emp2.setOrganization(org2);
        emp2 = employeeRepository.save(emp2);

        // Policy 1: Target Slab Sales Incentive
        policy1 = IncentivePolicy.builder()
                .organization(org1)
                .policyCode("SALES-SLAB-2026")
                .policyName("2026 Sales Slab Incentive")
                .incentiveType(IncentiveType.SALES)
                .calculationMethod(IncentiveCalculationMethod.TARGET_SLAB)
                .paymentFrequency(IncentivePaymentFrequency.MONTHLY)
                .status(IncentivePolicyStatus.ACTIVE)
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .targetValue(BigDecimal.valueOf(100000))
                .targetSlabs(List.of(
                        new IncentiveSlabTierDto(BigDecimal.valueOf(80), BigDecimal.valueOf(100), BigDecimal.valueOf(3000), null),
                        new IncentiveSlabTierDto(BigDecimal.valueOf(100), BigDecimal.valueOf(120), BigDecimal.valueOf(6000), null),
                        new IncentiveSlabTierDto(BigDecimal.valueOf(120), BigDecimal.valueOf(200), BigDecimal.valueOf(10000), null)
                ))
                .build();
        policy1 = policyRepository.save(policy1);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void testEndToEndIncentiveLifecycle() {
        TenantContext.setCurrentTenant(org1.getId());

        IncentiveCalculateRequest calcReq = new IncentiveCalculateRequest();
        calcReq.setEmployeeId(emp1.getId());
        calcReq.setPolicyId(policy1.getId());
        calcReq.setPeriodStart(periodStart);
        calcReq.setPeriodEnd(periodEnd);
        calcReq.setAchievedValue(BigDecimal.valueOf(110000)); // 110% achievement -> falls in 100-120 slab -> 6,000

        // 1. Preview Calculation (Stateless)
        IncentivePreviewResponse preview = calculationService.previewIncentive(calcReq);
        assertNotNull(preview);
        assertTrue(preview.isEligible());
        assertEquals(new BigDecimal("6000.00"), preview.getCalculatedAmount());

        // 2. Persist Calculation
        IncentiveRecordResponse recordResp = calculationService.calculateAndPersist(calcReq);
        assertNotNull(recordResp);
        Long recordId = recordResp.getId();
        assertEquals(IncentiveStatus.CALCULATED, recordResp.getStatus());
        assertEquals(0, new BigDecimal("6000.00").compareTo(recordResp.getCalculatedAmount()));

        // 3. Manager Adjustment (Adjust down to 5,000)
        IncentiveAdjustmentRequest adjReq = new IncentiveAdjustmentRequest(
                BigDecimal.valueOf(5000), "Slight deduction due to customer return"
        );
        IncentiveRecordResponse adjustedResp = adjustmentService.adjustIncentive(recordId, adjReq);
        assertEquals(IncentiveStatus.ADJUSTED, adjustedResp.getStatus());
        // Original values preserved
        assertEquals(0, new BigDecimal("6000.00").compareTo(adjustedResp.getCalculatedAmount()));
        // Adjusted values updated
        assertEquals(0, new BigDecimal("5000.00").compareTo(adjustedResp.getAdjustedAmount()));

        // 4. Submit for Central Approval
        IncentiveRecordResponse submittedResp = workflowService.submitForApproval(recordId);
        assertEquals(IncentiveStatus.PENDING_APPROVAL, submittedResp.getStatus());

        // 5. Complete Central Approval Workflow (Simulate APPROVED event)
        ApprovalWorkflowCompletedEvent approvedEvent = new ApprovalWorkflowCompletedEvent(
                this, submittedResp.getWorkflowInstanceId(), WorkflowType.INCENTIVE_REQUEST,
                "INCENTIVE_RECORD", String.valueOf(recordId), org1.getId(), ApprovalStatus.APPROVED
        );
        approvalEventListener.onApprovalWorkflowCompleted(approvedEvent);

        IncentiveRecord approvedRecord = recordRepository.findById(recordId).orElseThrow();
        assertEquals(IncentiveStatus.APPROVED, approvedRecord.getStatus());
        assertEquals(0, new BigDecimal("5000.00").compareTo(approvedRecord.getApprovedAmount()));
        assertEquals(IncentivePayrollStatus.PENDING, approvedRecord.getPayrollStatus());

        // 6. Query Eligible Records for Payroll Run
        List<IncentiveRecordResponse> eligible = payrollIntegrationService.getEligibleIncentiveRecords(
                emp1.getId(), periodStart, periodEnd
        );
        assertEquals(1, eligible.size());
        assertEquals(recordId, eligible.get(0).getId());

        // 7. Post to Payroll Run
        Long payrollRunId = 7788L;
        BigDecimal totalPosted = payrollIntegrationService.postIncentivesToPayrollRun(
                org1.getId(), emp1.getId(), periodStart, periodEnd, payrollRunId
        );
        assertEquals(0, new BigDecimal("5000.00").compareTo(totalPosted));

        IncentiveRecord postedRecord = recordRepository.findById(recordId).orElseThrow();
        assertEquals(IncentiveStatus.POSTED_TO_PAYROLL, postedRecord.getStatus());
        assertEquals(IncentivePayrollStatus.POSTED, postedRecord.getPayrollStatus());
        assertEquals(payrollRunId, postedRecord.getPayrollRunId());

        // 8. Verify Idempotency - Repeated posting does NOT double count
        BigDecimal secondPosted = payrollIntegrationService.postIncentivesToPayrollRun(
                org1.getId(), emp1.getId(), periodStart, periodEnd, payrollRunId
        );
        assertEquals(0, BigDecimal.ZERO.compareTo(secondPosted));
    }

    @Test
    void testTenantIsolation_CrossTenantAccessForbidden() {
        TenantContext.setCurrentTenant(org1.getId());

        IncentiveCalculateRequest calcReq = new IncentiveCalculateRequest();
        calcReq.setEmployeeId(emp1.getId());
        calcReq.setPolicyId(policy1.getId());
        calcReq.setPeriodStart(periodStart);
        calcReq.setPeriodEnd(periodEnd);
        calcReq.setAchievedValue(BigDecimal.valueOf(110000));

        IncentiveRecordResponse record = calculationService.calculateAndPersist(calcReq);

        // Switch to Org 2 (Tenant B)
        TenantContext.setCurrentTenant(org2.getId());

        // Org 2 cannot adjust Org 1 incentive record
        assertThrows(RuntimeException.class, () ->
                adjustmentService.adjustIncentive(record.getId(), new IncentiveAdjustmentRequest(BigDecimal.valueOf(1000), "Malicious adjust"))
        );

        // Org 2 cannot submit Org 1 incentive record
        assertThrows(RuntimeException.class, () ->
                workflowService.submitForApproval(record.getId())
        );

        // Org 2 cannot post Org 1 incentive record to its payroll
        BigDecimal posted = payrollIntegrationService.postIncentivesToPayrollRun(
                org2.getId(), emp1.getId(), periodStart, periodEnd, 9999L
        );
        assertEquals(BigDecimal.ZERO, posted);
    }
}
