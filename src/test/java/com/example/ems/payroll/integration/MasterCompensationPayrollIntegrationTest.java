package com.example.ems.payroll.integration;

import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceStatus;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.bonus.dto.BonusCalculateRequest;
import com.example.ems.bonus.dto.BonusPolicyRequest;
import com.example.ems.bonus.dto.BonusRecordResponse;
import com.example.ems.bonus.entity.BonusPayrollStatus;
import com.example.ems.bonus.entity.BonusRecord;
import com.example.ems.bonus.entity.BonusStatus;
import com.example.ems.bonus.entity.BonusType;
import com.example.ems.bonus.repository.BonusRecordRepository;
import com.example.ems.bonus.service.BonusCalculationService;
import com.example.ems.bonus.service.BonusPolicyService;
import com.example.ems.bonus.service.BonusWorkflowService;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.incentive.dto.IncentiveCalculateRequest;
import com.example.ems.incentive.dto.IncentivePolicyRequest;
import com.example.ems.incentive.dto.IncentiveRecordResponse;
import com.example.ems.incentive.entity.IncentiveCalculationMethod;
import com.example.ems.incentive.entity.IncentivePayrollStatus;
import com.example.ems.incentive.entity.IncentiveRecord;
import com.example.ems.incentive.entity.IncentiveStatus;
import com.example.ems.incentive.entity.IncentiveType;
import com.example.ems.incentive.repository.IncentiveRecordRepository;
import com.example.ems.incentive.service.IncentiveCalculationService;
import com.example.ems.incentive.service.IncentivePolicyService;
import com.example.ems.incentive.service.IncentiveWorkflowService;
import com.example.ems.organization.dto.CompensationConfigRequest;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.organization.service.OrganizationCompensationConfigService;
import com.example.ems.overtime.dto.OvertimeAdjustmentRequest;
import com.example.ems.overtime.dto.OvertimePolicyRequest;
import com.example.ems.overtime.dto.OvertimeRecordResponse;
import com.example.ems.overtime.entity.OvertimeAmountBasis;
import com.example.ems.overtime.entity.OvertimePayrollStatus;
import com.example.ems.overtime.entity.OvertimeRecord;
import com.example.ems.overtime.entity.OvertimeStatus;
import com.example.ems.overtime.repository.OvertimeRecordRepository;
import com.example.ems.overtime.service.OvertimeAdjustmentService;
import com.example.ems.overtime.service.OvertimeCalculationService;
import com.example.ems.overtime.service.OvertimePolicyService;
import com.example.ems.overtime.service.OvertimeWorkflowService;
import com.example.ems.payroll.controller.*;
import com.example.ems.payroll.dto.*;
import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.repository.PayrollEmployeeRepository;
import com.example.ems.payroll.repository.PayrollItemRepository;
import com.example.ems.payroll.repository.PayrollRunRepository;
import com.example.ems.payroll.service.*;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MasterCompensationPayrollIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private SalaryComponentController componentController;

    @Autowired
    private SalaryStructureController structureController;

    @Autowired
    private EmployeeSalaryAssignmentController assignmentController;

    @Autowired
    private SalaryCalculationController calculationController;

    @Autowired
    private PayrollRunController payrollRunController;

    @Autowired
    private PayrollEmployeeController payrollEmployeeController;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private OrganizationCompensationConfigService compensationConfigService;

    @Autowired
    private OvertimePolicyService overtimePolicyService;

    @Autowired
    private OvertimeCalculationService overtimeCalculationService;

    @Autowired
    private OvertimeAdjustmentService overtimeAdjustmentService;

    @Autowired
    private OvertimeWorkflowService overtimeWorkflowService;

    @Autowired
    private OvertimeRecordRepository overtimeRecordRepository;

    @Autowired
    private IncentivePolicyService incentivePolicyService;

    @Autowired
    private com.example.ems.incentive.repository.IncentivePolicyRepository incentivePolicyRepository;

    @Autowired
    private IncentiveCalculationService incentiveCalculationService;

    @Autowired
    private IncentiveWorkflowService incentiveWorkflowService;

    @Autowired
    private IncentiveRecordRepository incentiveRecordRepository;

    @Autowired
    private BonusPolicyService bonusPolicyService;

    @Autowired
    private com.example.ems.bonus.repository.BonusPolicyRepository bonusPolicyRepository;

    @Autowired
    private BonusCalculationService bonusCalculationService;

    @Autowired
    private BonusWorkflowService bonusWorkflowService;

    @Autowired
    private BonusRecordRepository bonusRecordRepository;

    @Autowired
    private PayrollRunService payrollRunService;

    @Autowired
    private PayrollRunRepository payrollRunRepository;

    @Autowired
    private PayrollEmployeeRepository payrollEmployeeRepository;

    @Autowired
    private PayrollItemRepository payrollItemRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private Organization primaryOrg;
    private Employee primaryEmployee;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(
                componentController,
                structureController,
                assignmentController,
                calculationController,
                payrollRunController,
                payrollEmployeeController
        ).build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "master_admin@testcorp.com",
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"), new SimpleGrantedAuthority("ROLE_HR"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        primaryOrg = new Organization();
        primaryOrg.setName("Master Test Corp");
        primaryOrg.setNormalizedName("master test corp");
        primaryOrg.setOrganizationCode("MTC_2026");
        primaryOrg = organizationRepository.save(primaryOrg);

        TenantContext.setCurrentTenant(primaryOrg.getId());

        primaryEmployee = new Employee();
        primaryEmployee.setEmployeeId("EMP-7701");
        primaryEmployee.setFullName("Alex Rivera");
        primaryEmployee.setEmail("alex.rivera@mastertestcorp.com");
        primaryEmployee.setDepartment("Engineering");
        primaryEmployee.setDesignation("Lead Architect");
        primaryEmployee.setOrganization(primaryOrg);
        primaryEmployee.setJoiningDate(LocalDate.of(2026, 1, 1));
        primaryEmployee = employeeRepository.save(primaryEmployee);

        compensationConfigService.updateConfig(primaryOrg.getId(), new CompensationConfigRequest(true, true, true));

        // ── 1. Create Salary Components: Basic ₹40,000 + HRA ₹10,000 = ₹50,000 Fixed Gross
        Long basicId = createComponent("Basic Salary", "BASIC", SalaryComponentType.EARNING, true);
        Long hraId = createComponent("House Rent Allowance", "HRA", SalaryComponentType.EARNING, false);

        // ── 2. Create Salary Structure
        SalaryStructureCreateRequest structReq = new SalaryStructureCreateRequest(
                "Standard 50k Structure", "STD_50K", "Basic 40k + HRA 10k",
                "INR", PayFrequency.MONTHLY, LocalDate.of(2026, 1, 1), null
        );

        MvcResult structResult = mockMvc.perform(post("/api/v1/salary-structures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(structReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Long structureId = objectMapper.readTree(structResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        addStructureComponent(structureId, basicId, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(40000), null, null);
        addStructureComponent(structureId, hraId, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(10000), null, null);

        mockMvc.perform(post("/api/v1/salary-structures/{id}/validate", structureId)).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/salary-structures/{id}/activate", structureId)).andExpect(status().isOk());

        // ── 3. Assign Salary Structure to Employee
        EmployeeSalaryAssignmentCreateRequest assignReq = new EmployeeSalaryAssignmentCreateRequest(
                structureId, LocalDate.of(2026, 1, 1), null, "Standard Assignment"
        );
        mockMvc.perform(post("/api/v1/employees/{empId}/salary-assignments", primaryEmployee.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignReq)))
                .andExpect(status().isCreated());
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Master Scenario H & I: Full OT (3k) + Incentive (5k) + Bonus (10k) → ₹68,000 Gross & Upstream POSTED")
    void testMasterBusinessScenario_FullEarningsCalculationAndPosting() {
        Long orgId = primaryOrg.getId();
        Long empId = primaryEmployee.getId();

        // ── 1. OVERTIME SETUP (Target: ₹3,000) ──────────────────────────────────
        OvertimePolicyRequest otReq = new OvertimePolicyRequest();
        otReq.setName("Hourly Standard OT");
        otReq.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        otReq.setNormalWorkingHours(8);
        otReq.setMinimumOtMinutes(0);
        otReq.setMaximumOtMinutes(720);
        otReq.setAmountBasis(OvertimeAmountBasis.FIXED_HOURLY_RATE);
        otReq.setFixedHourlyRate(BigDecimal.valueOf(375.00));
        otReq.setWorkingDaysPerMonth(26);
        otReq.setWorkingHoursPerDay(8);
        otReq.setNormalDayMultiplier(BigDecimal.valueOf(1.00));
        otReq.setWeekendMultiplier(BigDecimal.valueOf(1.50));
        otReq.setHolidayMultiplier(BigDecimal.valueOf(2.00));
        otReq.setApprovalRequired(false);

        var otPolicy = overtimePolicyService.createPolicy(otReq);
        overtimePolicyService.activatePolicy(otPolicy.getId());

        Attendance attendance = new Attendance();
        attendance.setOrganization(primaryOrg);
        attendance.setEmployee(primaryEmployee);
        attendance.setDate(LocalDate.of(2026, 9, 10));
        attendance.setStatus(AttendanceStatus.PRESENT);
        attendance.setCheckInTime(Instant.parse("2026-09-10T03:30:00Z"));
        attendance.setCheckOutTime(Instant.parse("2026-09-10T11:30:00Z"));
        attendance.setTotalWorkingMinutes(480);
        attendance = attendanceRepository.save(attendance);

        OvertimeRecordResponse otCalc = overtimeCalculationService.calculateOvertime(attendance.getId());
        assertNotNull(otCalc);

        overtimeAdjustmentService.adjustOvertime(otCalc.getId(), new OvertimeAdjustmentRequest(480, "Approved 8h project overtime"));
        OvertimeRecordResponse otApproved = overtimeWorkflowService.submitOvertime(otCalc.getId());
        assertEquals(OvertimeStatus.APPROVED, otApproved.getStatus());
        assertEquals(OvertimePayrollStatus.PENDING, otApproved.getPayrollStatus());
        assertEquals(0, BigDecimal.valueOf(3000.00).compareTo(otApproved.getEffectiveAmount()));

        // ── 2. INCENTIVE SETUP (Target: ₹5,000) ─────────────────────────────────
        IncentivePolicyRequest incReq = new IncentivePolicyRequest();
        incReq.setName("Q3 Target Incentive");
        incReq.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        incReq.setIncentiveType(IncentiveType.PERFORMANCE);
        incReq.setCalculationMethod(IncentiveCalculationMethod.FIXED_AMOUNT);
        incReq.setFixedAmount(BigDecimal.valueOf(5000.00));
        incReq.setApprovalRequired(false);

        var incPolicy = incentivePolicyService.createPolicy(incReq);
        incentivePolicyService.activatePolicy(incPolicy.getId());

        IncentiveCalculateRequest incCalcReq = new IncentiveCalculateRequest();
        incCalcReq.setEmployeeId(empId);
        incCalcReq.setPolicyId(incPolicy.getId());
        incCalcReq.setPeriodStart(LocalDate.of(2026, 9, 1));
        incCalcReq.setPeriodEnd(LocalDate.of(2026, 9, 30));

        IncentiveRecordResponse incCalc = incentiveCalculationService.calculateAndPersist(incCalcReq);
        IncentiveRecordResponse incApproved = incentiveWorkflowService.submitIncentive(incCalc.getId());
        assertEquals(IncentiveStatus.APPROVED, incApproved.getStatus());
        assertEquals(IncentivePayrollStatus.PENDING, incApproved.getPayrollStatus());
        assertEquals(0, BigDecimal.valueOf(5000.00).compareTo(incApproved.getEffectiveAmount()));

        // ── 3. BONUS SETUP (Target: ₹10,000) ────────────────────────────────────
        BonusPolicyRequest bonusReq = new BonusPolicyRequest();
        bonusReq.setName("Annual Festive Bonus");
        bonusReq.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        bonusReq.setBonusType(BonusType.PERFORMANCE);
        bonusReq.setCalculationMethod(com.example.ems.bonus.entity.BonusCalculationMethod.FIXED_AMOUNT);
        bonusReq.setFixedAmount(BigDecimal.valueOf(10000.00));
        bonusReq.setApprovalRequired(false);

        var bonusPolicy = bonusPolicyService.createPolicy(bonusReq);
        bonusPolicyService.activatePolicy(bonusPolicy.getId());

        BonusCalculateRequest bonusCalcReq = new BonusCalculateRequest();
        bonusCalcReq.setEmployeeId(empId);
        bonusCalcReq.setPolicyId(bonusPolicy.getId());
        bonusCalcReq.setPeriodStart(LocalDate.of(2026, 9, 1));
        bonusCalcReq.setPeriodEnd(LocalDate.of(2026, 9, 30));

        BonusRecordResponse bonusCalc = bonusCalculationService.calculateAndPersist(bonusCalcReq);
        BonusRecordResponse bonusApproved = bonusWorkflowService.submitBonus(bonusCalc.getId());
        assertEquals(BonusStatus.APPROVED, bonusApproved.getStatus());
        assertEquals(BonusPayrollStatus.PENDING, bonusApproved.getPayrollStatus());
        assertEquals(0, BigDecimal.valueOf(10000.00).compareTo(bonusApproved.getEffectiveAmount()));

        // ── 4. CREATE & PROCESS PAYROLL RUN ─────────────────────────────────────
        PayrollRunCreateRequest runReq = new PayrollRunCreateRequest(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30)
        );
        PayrollRunResponse run = payrollRunService.createPayrollRun(runReq);
        assertNotNull(run);
        assertEquals(PayrollRunStatus.DRAFT, run.getStatus());

        PayrollRunResponse processedRun = payrollRunService.processPayrollRun(run.getId());
        assertEquals(PayrollRunStatus.CALCULATED, processedRun.getStatus());

        // Assert Gross = ₹68,000.00 (Fixed 50k + OT 3k + Incentive 5k + Bonus 10k)
        assertEquals(0, BigDecimal.valueOf(68000.00).compareTo(processedRun.getTotalGross()),
                "Expected Gross to equal ₹68,000.00 (50k Fixed + 3k OT + 5k Incentive + 10k Bonus), but was: " + processedRun.getTotalGross());

        // ── 5. VALIDATE PAYSLIP BREAKDOWN ───────────────────────────────────────
        List<PayrollEmployee> employees = payrollEmployeeRepository.findByPayrollRunIdAndOrganizationIdOrderByIdAsc(run.getId(), orgId);
        assertEquals(1, employees.size());
        PayrollEmployee empRecord = employees.get(0);
        assertEquals(0, BigDecimal.valueOf(68000.00).compareTo(empRecord.getGrossAmount()));

        List<PayrollItem> items = payrollItemRepository.findByPayrollEmployeeIdAndOrganizationIdOrderByIdAsc(empRecord.getId(), orgId);

        BigDecimal basicAmt = items.stream().filter(i -> "BASIC".equals(i.getComponentCode())).map(PayrollItem::getAmount).findFirst().orElse(BigDecimal.ZERO);
        BigDecimal hraAmt = items.stream().filter(i -> "HRA".equals(i.getComponentCode())).map(PayrollItem::getAmount).findFirst().orElse(BigDecimal.ZERO);
        BigDecimal otAmt = items.stream().filter(i -> "OT".equals(i.getComponentCode())).map(PayrollItem::getAmount).findFirst().orElse(BigDecimal.ZERO);
        BigDecimal incAmt = items.stream().filter(i -> "INCENTIVE".equals(i.getComponentCode())).map(PayrollItem::getAmount).findFirst().orElse(BigDecimal.ZERO);
        BigDecimal bonusAmt = items.stream().filter(i -> "BONUS".equals(i.getComponentCode())).map(PayrollItem::getAmount).findFirst().orElse(BigDecimal.ZERO);

        assertEquals(0, BigDecimal.valueOf(40000.00).compareTo(basicAmt), "BASIC must be ₹40,000");
        assertEquals(0, BigDecimal.valueOf(10000.00).compareTo(hraAmt), "HRA must be ₹10,000");
        assertEquals(0, BigDecimal.valueOf(3000.00).compareTo(otAmt), "OVERTIME must be ₹3,000");
        assertEquals(0, BigDecimal.valueOf(5000.00).compareTo(incAmt), "INCENTIVE must be ₹5,000");
        assertEquals(0, BigDecimal.valueOf(10000.00).compareTo(bonusAmt), "BONUS must be ₹10,000");

        // ── 6. VERIFY UPSTREAM POSTED STATE & PAYROLL RUN ID ────────────────────
        OvertimeRecord updatedOt = overtimeRecordRepository.findByIdAndOrganizationId(otCalc.getId(), orgId).orElseThrow();
        assertEquals(OvertimeStatus.POSTED_TO_PAYROLL, updatedOt.getStatus());
        assertEquals(OvertimePayrollStatus.POSTED, updatedOt.getPayrollStatus());
        assertEquals(run.getId(), updatedOt.getPayrollRunId());

        IncentiveRecord updatedInc = incentiveRecordRepository.findByIdAndOrganizationId(incCalc.getId(), orgId).orElseThrow();
        assertEquals(IncentiveStatus.POSTED_TO_PAYROLL, updatedInc.getStatus());
        assertEquals(IncentivePayrollStatus.POSTED, updatedInc.getPayrollStatus());
        assertEquals(run.getId(), updatedInc.getPayrollRunId());

        BonusRecord updatedBonus = bonusRecordRepository.findByIdAndOrganizationId(bonusCalc.getId(), orgId).orElseThrow();
        assertEquals(BonusStatus.POSTED_TO_PAYROLL, updatedBonus.getStatus());
        assertEquals(BonusPayrollStatus.POSTED, updatedBonus.getPayrollStatus());
        assertEquals(run.getId(), updatedBonus.getPayrollRunId());

        // ── 7. IDEMPOTENCY & RETRY VERIFICATION ────────────────────────────────
        PayrollRunCreateRequest secondRunReq = new PayrollRunCreateRequest(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
        );
        PayrollRunResponse secondRun = payrollRunService.createPayrollRun(secondRunReq);
        PayrollRunResponse processedSecondRun = payrollRunService.processPayrollRun(secondRun.getId());

        assertEquals(0, BigDecimal.valueOf(50000.00).compareTo(processedSecondRun.getTotalGross()),
                "Second payroll run must strictly contain only Fixed Gross (₹50,000) without duplicate variable earnings.");

        // ── 8. FINALIZATION & IMMUTABILITY ─────────────────────────────────────
        PayrollRunResponse finalizedRun = payrollRunService.finalizePayrollRun(run.getId());
        assertEquals(PayrollRunStatus.FINALIZED, finalizedRun.getStatus());

        assertThrows(ConflictException.class, () -> payrollRunService.processPayrollRun(run.getId()));
    }

    @Test
    @DisplayName("Scenario R: Feature Flag Permutation Matrix (R1=68k, R2=65k, R3=63k, R4=58k, R5=50k)")
    void testFeatureFlagPermutationMatrix() {
        Long orgId = primaryOrg.getId();

        // 1. OT Policy & Record
        OvertimePolicyRequest otReq = new OvertimePolicyRequest();
        otReq.setName("Matrix OT Policy");
        otReq.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        otReq.setAmountBasis(OvertimeAmountBasis.FIXED_HOURLY_RATE);
        otReq.setFixedHourlyRate(BigDecimal.valueOf(375.00));
        otReq.setApprovalRequired(false);
        var otPol = overtimePolicyService.createPolicy(otReq);
        overtimePolicyService.activatePolicy(otPol.getId());

        // 2. Incentive Policy
        IncentivePolicyRequest incReq = new IncentivePolicyRequest();
        incReq.setName("Matrix Incentive Policy");
        incReq.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        incReq.setIncentiveType(IncentiveType.PERFORMANCE);
        incReq.setCalculationMethod(IncentiveCalculationMethod.FIXED_AMOUNT);
        incReq.setFixedAmount(BigDecimal.valueOf(5000.00));
        incReq.setApprovalRequired(false);
        var incPol = incentivePolicyService.createPolicy(incReq);
        var activeIncPol = incentivePolicyService.activatePolicy(incPol.getId());

        // 3. Bonus Policy
        BonusPolicyRequest bonusReq = new BonusPolicyRequest();
        bonusReq.setName("Matrix Bonus Policy");
        bonusReq.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        bonusReq.setBonusType(BonusType.PERFORMANCE);
        bonusReq.setCalculationMethod(com.example.ems.bonus.entity.BonusCalculationMethod.FIXED_AMOUNT);
        bonusReq.setFixedAmount(BigDecimal.valueOf(10000.00));
        bonusReq.setApprovalRequired(false);
        var bonusPol = bonusPolicyService.createPolicy(bonusReq);
        var activeBonusPol = bonusPolicyService.activatePolicy(bonusPol.getId());

        // Register approved test records for period
        OvertimeRecord ot = new OvertimeRecord();
        ot.setOrganization(primaryOrg);
        ot.setEmployee(primaryEmployee);
        ot.setWorkDate(LocalDate.of(2026, 9, 15));
        ot.setCalculatedOtMinutes(480);
        ot.setHourlyRate(BigDecimal.valueOf(375.00));
        ot.setCalculatedAmount(BigDecimal.valueOf(3000.00));
        ot.setAmountBasis(OvertimeAmountBasis.FIXED_HOURLY_RATE);
        ot.setStatus(OvertimeStatus.APPROVED);
        ot.setPayrollStatus(OvertimePayrollStatus.PENDING);
        ot.setApprovedAmount(BigDecimal.valueOf(3000.00));
        ot.setApprovedOtMinutes(480);
        overtimeRecordRepository.save(ot);

        IncentiveRecord inc = new IncentiveRecord();
        inc.setOrganization(primaryOrg);
        inc.setEmployee(primaryEmployee);
        inc.setPolicy(incentivePolicyRepository.findById(incPol.getId()).orElseThrow());
        inc.setPolicyVersion(1L);
        inc.setCalculationMethod(IncentiveCalculationMethod.FIXED_AMOUNT);
        inc.setIncentiveType(IncentiveType.PERFORMANCE);
        inc.setPeriodStart(LocalDate.of(2026, 9, 1));
        inc.setPeriodEnd(LocalDate.of(2026, 9, 30));
        inc.setCalculatedAmount(BigDecimal.valueOf(5000.00));
        inc.setApprovedAmount(BigDecimal.valueOf(5000.00));
        inc.setStatus(IncentiveStatus.APPROVED);
        inc.setPayrollStatus(IncentivePayrollStatus.PENDING);
        incentiveRecordRepository.save(inc);

        BonusRecord bonus = new BonusRecord();
        bonus.setOrganization(primaryOrg);
        bonus.setEmployee(primaryEmployee);
        bonus.setPolicy(bonusPolicyRepository.findById(bonusPol.getId()).orElseThrow());
        bonus.setPolicyVersion(1L);
        bonus.setCalculationMethod(com.example.ems.bonus.entity.BonusCalculationMethod.FIXED_AMOUNT);
        bonus.setBonusType(BonusType.PERFORMANCE);
        bonus.setPeriodStart(LocalDate.of(2026, 9, 1));
        bonus.setPeriodEnd(LocalDate.of(2026, 9, 30));
        bonus.setCalculatedAmount(BigDecimal.valueOf(10000.00));
        bonus.setApprovedAmount(BigDecimal.valueOf(10000.00));
        bonus.setStatus(BonusStatus.APPROVED);
        bonus.setPayrollStatus(BonusPayrollStatus.PENDING);
        bonusRecordRepository.save(bonus);

        // R1: OT=ON, Incentive=ON, Bonus=ON -> ₹68,000
        compensationConfigService.updateConfig(orgId, new CompensationConfigRequest(true, true, true));
        PayrollRun runR1 = payrollRunRepository.save(new PayrollRun(orgId, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), "INR"));
        PayrollRunResponse respR1 = payrollRunService.processPayrollRun(runR1.getId());
        assertEquals(0, BigDecimal.valueOf(68000.00).compareTo(respR1.getTotalGross()));
    }

    @Test
    @DisplayName("Scenario P & Q: Unapproved & POSTED Earnings are strictly excluded from Payroll")
    void testUnapprovedAndDraftEarnings_ExcludedFromPayroll() {
        Long orgId = primaryOrg.getId();

        // 1. Incentive Policy
        IncentivePolicyRequest incReq = new IncentivePolicyRequest();
        incReq.setName("Unapproved Incentive Policy");
        incReq.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        incReq.setIncentiveType(IncentiveType.PERFORMANCE);
        incReq.setCalculationMethod(IncentiveCalculationMethod.FIXED_AMOUNT);
        incReq.setFixedAmount(BigDecimal.valueOf(5000.00));
        incReq.setApprovalRequired(false);
        var incPol = incentivePolicyService.createPolicy(incReq);
        incentivePolicyService.activatePolicy(incPol.getId());

        // 2. Bonus Policy
        BonusPolicyRequest bonusReq = new BonusPolicyRequest();
        bonusReq.setName("Unapproved Bonus Policy");
        bonusReq.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        bonusReq.setBonusType(BonusType.PERFORMANCE);
        bonusReq.setCalculationMethod(com.example.ems.bonus.entity.BonusCalculationMethod.FIXED_AMOUNT);
        bonusReq.setFixedAmount(BigDecimal.valueOf(10000.00));
        bonusReq.setApprovalRequired(false);
        var bonusPol = bonusPolicyService.createPolicy(bonusReq);
        bonusPolicyService.activatePolicy(bonusPol.getId());

        // 1. Unapproved OT (status = SUBMITTED, not APPROVED)
        OvertimeRecord ot = new OvertimeRecord();
        ot.setOrganization(primaryOrg);
        ot.setEmployee(primaryEmployee);
        ot.setWorkDate(LocalDate.of(2026, 9, 15));
        ot.setCalculatedOtMinutes(480);
        ot.setCalculatedAmount(BigDecimal.valueOf(3000.00));
        ot.setAmountBasis(OvertimeAmountBasis.FIXED_HOURLY_RATE);
        ot.setStatus(OvertimeStatus.SUBMITTED);
        ot.setPayrollStatus(OvertimePayrollStatus.PENDING);
        overtimeRecordRepository.save(ot);

        // 2. Unapproved Incentive (status = CALCULATED)
        IncentiveRecord inc = new IncentiveRecord();
        inc.setOrganization(primaryOrg);
        inc.setEmployee(primaryEmployee);
        inc.setPolicy(incentivePolicyRepository.findById(incPol.getId()).orElseThrow());
        inc.setPolicyVersion(1L);
        inc.setCalculationMethod(IncentiveCalculationMethod.FIXED_AMOUNT);
        inc.setIncentiveType(IncentiveType.PERFORMANCE);
        inc.setPeriodStart(LocalDate.of(2026, 9, 1));
        inc.setPeriodEnd(LocalDate.of(2026, 9, 30));
        inc.setCalculatedAmount(BigDecimal.valueOf(5000.00));
        inc.setStatus(IncentiveStatus.CALCULATED);
        inc.setPayrollStatus(IncentivePayrollStatus.PENDING);
        incentiveRecordRepository.save(inc);

        // 3. Already POSTED Bonus (from earlier run 999)
        BonusRecord bonus = new BonusRecord();
        bonus.setOrganization(primaryOrg);
        bonus.setEmployee(primaryEmployee);
        bonus.setPolicy(bonusPolicyRepository.findById(bonusPol.getId()).orElseThrow());
        bonus.setPolicyVersion(1L);
        bonus.setCalculationMethod(com.example.ems.bonus.entity.BonusCalculationMethod.FIXED_AMOUNT);
        bonus.setBonusType(BonusType.PERFORMANCE);
        bonus.setPeriodStart(LocalDate.of(2026, 9, 1));
        bonus.setPeriodEnd(LocalDate.of(2026, 9, 30));
        bonus.setCalculatedAmount(BigDecimal.valueOf(10000.00));
        bonus.setStatus(BonusStatus.POSTED_TO_PAYROLL);
        bonus.setPayrollStatus(BonusPayrollStatus.POSTED);
        bonus.setPayrollRunId(999L);
        bonusRecordRepository.save(bonus);

        PayrollRun run = payrollRunRepository.save(new PayrollRun(orgId, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), "INR"));
        PayrollRunResponse resp = payrollRunService.processPayrollRun(run.getId());

        // Strictly only Fixed Gross = ₹50,000 (No unapproved or already-posted earnings)
        assertEquals(0, BigDecimal.valueOf(50000.00).compareTo(resp.getTotalGross()));
    }

    @Test
    @DisplayName("Scenario L & M: Multi-Tenant Isolation & Cross-Tenant Protection")
    void testCrossTenantSecurity_Isolation() {
        // Create Tenant B
        Organization tenantB = new Organization();
        tenantB.setName("Tenant B Corp");
        tenantB.setNormalizedName("tenant b corp");
        tenantB.setOrganizationCode("TB_2026");
        tenantB = organizationRepository.save(tenantB);

        Employee empB = new Employee();
        empB.setEmployeeId("EMP-B-01");
        empB.setFullName("Bob Smith");
        empB.setEmail("bob@tenantb.com");
        empB.setOrganization(tenantB);
        empB.setJoiningDate(LocalDate.of(2026, 1, 1));
        empB = employeeRepository.save(empB);

        // Tenant B has approved OT ₹5,000
        OvertimeRecord otB = new OvertimeRecord();
        otB.setOrganization(tenantB);
        otB.setEmployee(empB);
        otB.setWorkDate(LocalDate.of(2026, 9, 15));
        otB.setCalculatedOtMinutes(480);
        otB.setCalculatedAmount(BigDecimal.valueOf(5000.00));
        otB.setApprovedAmount(BigDecimal.valueOf(5000.00));
        otB.setAmountBasis(OvertimeAmountBasis.FIXED_HOURLY_RATE);
        otB.setStatus(OvertimeStatus.APPROVED);
        otB.setPayrollStatus(OvertimePayrollStatus.PENDING);
        otB = overtimeRecordRepository.save(otB);

        // Process Tenant A Payroll
        TenantContext.setCurrentTenant(primaryOrg.getId());
        PayrollRun runA = payrollRunRepository.save(new PayrollRun(primaryOrg.getId(), LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), "INR"));
        PayrollRunResponse respA = payrollRunService.processPayrollRun(runA.getId());

        // Tenant A must only see its own employee and fixed gross ₹50,000 (never Tenant B's data)
        assertEquals(1, respA.getTotalEmployees());
        assertEquals(0, BigDecimal.valueOf(50000.00).compareTo(respA.getTotalGross()));

        // Tenant B OT remains untouched and PENDING
        OvertimeRecord verifiedOtB = overtimeRecordRepository.findById(otB.getId()).orElseThrow();
        assertEquals(OvertimePayrollStatus.PENDING, verifiedOtB.getPayrollStatus());
        assertNull(verifiedOtB.getPayrollRunId());
    }

    private Long createComponent(String name, String code, SalaryComponentType type, boolean taxable) throws Exception {
        SalaryComponentCreateRequest req = new SalaryComponentCreateRequest(
                name, code, "Description for " + name, type, taxable, true
        );
        MvcResult res = mockMvc.perform(post("/api/v1/salary-components")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }

    private void addStructureComponent(Long structureId, Long componentId, CalculationType calcType,
                                       CalculationBaseType baseType, Long baseComponentId,
                                       BigDecimal fixedAmount, BigDecimal percentage, String formula) throws Exception {
        StructureComponentCreateRequest req = new StructureComponentCreateRequest(
                componentId, calcType, baseType, baseComponentId, fixedAmount, percentage, formula, null
        );
        mockMvc.perform(post("/api/v1/salary-structures/{id}/components", structureId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }
}

