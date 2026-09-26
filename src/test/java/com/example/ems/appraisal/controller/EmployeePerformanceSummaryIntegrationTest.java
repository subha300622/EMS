package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.EmployeePerformanceSummaryDto;
import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.appraisal.entity.AppraisalCycle;
import com.example.ems.appraisal.entity.AppraisalStatus;
import com.example.ems.appraisal.repository.AppraisalCycleRepository;
import com.example.ems.appraisal.repository.AppraisalRepository;
import com.example.ems.appraisal.service.AppraisalEvaluationService;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceStatus;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.goal.domain.Goal;
import com.example.ems.goal.repository.GoalRepository;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.entity.LeaveType;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.leave.repository.LeaveTypeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.service.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class EmployeePerformanceSummaryIntegrationTest {

    @Autowired
    private AppraisalEvaluationController evaluationController;

    @Autowired
    private AppraisalEvaluationService evaluationService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AppraisalRepository appraisalRepository;

    @Autowired
    private AppraisalCycleRepository cycleRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    @Qualifier("enterpriseGoalRepository")
    private GoalRepository goalRepository;

    @Autowired
    private com.example.ems.performance.repository.PerformanceReviewRecordRepository performanceReviewRecordRepository;

    @Autowired
    private com.example.ems.performance.repository.PerformanceReviewCycleRepository performanceReviewCycleRepository;

    @Autowired
    private com.example.ems.security.rls.PostgresRlsSessionBinder rlsSessionBinder;

    @Autowired
    private JwtService jwtService;

    private Organization orgA;
    private Organization orgB;
    private Employee empA;
    private Employee managerA;
    private Employee empB;
    private User managerUser;
    private String managerToken;

    @BeforeEach
    public void setup() {
        long ts = System.currentTimeMillis();

        // 1. Setup Organizations
        orgA = new Organization();
        orgA.setName("Summary Test Org A " + ts);
        orgA.setOrganizationCode("ORG-A-" + ts);
        orgA = organizationRepository.save(orgA);

        orgB = new Organization();
        orgB.setName("Summary Test Org B " + ts);
        orgB.setOrganizationCode("ORG-B-" + ts);
        orgB = organizationRepository.save(orgB);

        // 2. Setup Roles
        Role managerRole = roleRepository.findByName("PLATFORM_ADMIN").orElseGet(() -> {
            Role r = new Role();
            r.setName("PLATFORM_ADMIN");
            r.setOrganization(orgA);
            return roleRepository.save(r);
        });

        // 3. Setup Employees
        managerA = new Employee();
        managerA.setOrganization(orgA);
        managerA.setFullName("Alice Manager");
        managerA.setEmail("alice.mgr." + ts + "@summaryorga.com");
        managerA.setEmployeeId("MGR" + ts % 100000);
        managerA.setDepartment("Engineering");
        managerA.setDesignation("Engineering Manager");
        managerA = employeeRepository.save(managerA);

        managerUser = new User();
        managerUser.setOrganization(orgA);
        managerUser.setWorkEmail(managerA.getEmail());
        managerUser.setRole(managerRole);
        managerUser = userRepository.save(managerUser);

        managerToken = "Bearer " + jwtService.generateAccessToken(String.valueOf(managerUser.getId()), managerUser.getWorkEmail(), "PLATFORM_ADMIN");

        empA = new Employee();
        empA.setOrganization(orgA);
        empA.setFullName("John Doe");
        empA.setEmail("john.doe." + ts + "@summaryorga.com");
        empA.setEmployeeId("EMP" + ts % 100000);
        empA.setDepartment("Engineering");
        empA.setDesignation("Software Engineer");
        empA.setManager(managerA);
        empA = employeeRepository.save(empA);

        empB = new Employee();
        empB.setOrganization(orgB);
        empB.setFullName("Bob Smith");
        empB.setEmail("bob.smith." + ts + "@summaryorgb.com");
        empB.setEmployeeId("EMPB" + ts % 100000);
        empB = employeeRepository.save(empB);

        TenantContext.setCurrentTenant(orgA.getId());
        rlsSessionBinder.bindCurrentTenant();
    }

    @AfterEach
    public void cleanup() {
        TenantContext.clear();
    }

    @Test
    public void testCompletePreReviewPerformanceSummaryFlow() {
        // Setup past completed appraisal
        AppraisalCycle pastCycle = new AppraisalCycle();
        pastCycle.setOrganization(orgA);
        pastCycle.setName("2025 Annual Performance Review");
        pastCycle.setStartDate(LocalDate.of(2025, 1, 1));
        pastCycle.setEndDate(LocalDate.of(2025, 12, 31));
        pastCycle.setStatus("COMPLETED");
        pastCycle = cycleRepository.save(pastCycle);

        Appraisal pastAppraisal = new Appraisal();
        pastAppraisal.setOrganization(orgA);
        pastAppraisal.setEmployee(empA);
        pastAppraisal.setCycle(pastCycle);
        pastAppraisal.setStatus(AppraisalStatus.PUBLISHED);
        pastAppraisal.setFinalRating(4.6);
        pastAppraisal.setPerformanceCategory("OUTSTANDING");
        pastAppraisal.setCompletedAt(LocalDateTime.of(2025, 12, 28, 10, 0));
        pastAppraisal.setPublishedAt(LocalDateTime.of(2025, 12, 29, 11, 0));
        appraisalRepository.save(pastAppraisal);

        // Setup active appraisal (in STAGE_REVIEW)
        AppraisalCycle currentCycle = new AppraisalCycle();
        currentCycle.setOrganization(orgA);
        currentCycle.setName("2026 Annual Performance Review");
        currentCycle.setStartDate(LocalDate.of(2026, 1, 1));
        currentCycle.setEndDate(LocalDate.of(2026, 12, 31));
        currentCycle.setStatus("ACTIVE");
        currentCycle = cycleRepository.save(currentCycle);

        Appraisal currentAppraisal = new Appraisal();
        currentAppraisal.setOrganization(orgA);
        currentAppraisal.setEmployee(empA);
        currentAppraisal.setCycle(currentCycle);
        currentAppraisal.setStatus(AppraisalStatus.STAGE_REVIEW);
        currentAppraisal.setCurrentStageOrder(2);
        currentAppraisal = appraisalRepository.save(currentAppraisal);

        // Setup Attendance records
        Attendance att1 = new Attendance();
        att1.setOrganization(orgA);
        att1.setEmployee(empA);
        att1.setDate(LocalDate.of(2026, 2, 2));
        att1.setStatus(AttendanceStatus.PRESENT);
        attendanceRepository.save(att1);

        Attendance att2 = new Attendance();
        att2.setOrganization(orgA);
        att2.setEmployee(empA);
        att2.setDate(LocalDate.of(2026, 2, 3));
        att2.setStatus(AttendanceStatus.LATE);
        attendanceRepository.save(att2);

        // Setup Leave records
        LeaveType leaveType = leaveTypeRepository.findByName("Casual Leave").orElseGet(() -> {
            LeaveType lt = new LeaveType();
            lt.setName("Casual Leave " + System.currentTimeMillis());
            lt.setDescription("Casual Leave Type");
            lt.setOrganization(orgA);
            return leaveTypeRepository.save(lt);
        });

        Leave leave = new Leave();
        leave.setOrganization(orgA);
        leave.setEmployee(empA);
        leave.setLeaveType(leaveType);
        leave.setStartDate(LocalDate.of(2026, 3, 10));
        leave.setEndDate(LocalDate.of(2026, 3, 12));
        leave.setDurationDays(3.0);
        leave.setStatus("APPROVED");
        leaveRepository.save(leave);

        // Setup Goals: 4 goals, 3 completed
        for (int i = 1; i <= 4; i++) {
            Goal goal = new Goal();
            goal.setOrganizationId(orgA.getId());
            goal.setOwnerId(empA.getId());
            goal.setGoalNumber("GOAL-" + i + "-" + System.currentTimeMillis() % 10000);
            goal.setGoalName("Goal " + i);
            if (i <= 3) {
                goal.setStatus("COMPLETED");
                goal.setProgress(100);
            } else {
                goal.setStatus("IN_PROGRESS");
                goal.setProgress(50);
            }
            goalRepository.save(goal);
        }

        // Execute via service
        EmployeePerformanceSummaryDto summary = evaluationService.getEmployeePerformanceSummary(empA.getId());

        assertNotNull(summary);
        assertEquals(empA.getId(), summary.getEmployeeId());
        assertEquals("John Doe", summary.getEmployeeName());
        assertEquals(empA.getEmployeeId(), summary.getEmployeeCode());
        assertEquals("Engineering", summary.getDepartment());
        assertEquals("Software Engineer", summary.getDesignation());

        // Previous Appraisals check
        assertNotNull(summary.getPreviousAppraisals());
        assertEquals(1, summary.getPreviousAppraisals().size());
        EmployeePerformanceSummaryDto.PreviousAppraisalDto prev = summary.getPreviousAppraisals().get(0);
        assertEquals("2025 Annual Performance Review", prev.getCycle());
        assertEquals(4.6, prev.getFinalScore());
        assertEquals(5.0, prev.getMaxScore());
        assertEquals("5_POINT", prev.getScoreScale());
        assertEquals("OUTSTANDING", prev.getRating());

        // Review Context check
        assertNotNull(summary.getReviewContext());
        assertTrue(summary.getReviewContext().isPreviousReviewAvailable());
        assertEquals(currentAppraisal.getId(), summary.getReviewContext().getCurrentReviewId());
        assertEquals("2026 Annual Performance Review", summary.getReviewContext().getCurrentCycleName());
        assertEquals(2, summary.getReviewContext().getCurrentStageOrder());
        assertEquals("STAGE_REVIEW", summary.getReviewContext().getAppraisalStatus());

        // Current Period Performance check
        EmployeePerformanceSummaryDto.CurrentPeriodPerformanceDto period = summary.getCurrentPeriodPerformance();
        assertNotNull(period);
        assertEquals(LocalDate.of(2026, 1, 1), period.getPeriodStart());
        assertNotNull(period.getPeriodEnd());
        assertEquals(3, period.getLeaveDays());
        assertEquals(4, period.getTotalGoals());
        assertEquals(3, period.getCompletedGoals());
        assertEquals(75.0, period.getGoalCompletionPercentage());
        // Since no performance calculation was run, KPI must be null
        assertNull(period.getKpiAchievementPercentage());
    }

    @Test
    public void testControllerEndpointAccessAndPermissions() {
        ResponseEntity<?> resp = evaluationController.getEmployeePerformanceSummary(managerToken, empA.getId());
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody() instanceof ApiResponse);

        ApiResponse<?> body = (ApiResponse<?>) resp.getBody();
        assertTrue(body.isSuccess());
        assertNotNull(body.getData());
    }

    @Test
    public void testMultiTenantBoundaryIsolation() {
        // With TenantContext set to Org A, querying empB (from Org B) must throw ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> {
            evaluationService.getEmployeePerformanceSummary(empB.getId());
        });

        // Switch to Org B
        TenantContext.setCurrentTenant(orgB.getId());
        assertThrows(ResourceNotFoundException.class, () -> {
            evaluationService.getEmployeePerformanceSummary(empA.getId());
        });
    }

    @Test
    public void testEmployeeWithNoPreviousAppraisals() {
        EmployeePerformanceSummaryDto summary = evaluationService.getEmployeePerformanceSummary(empA.getId());
        assertNotNull(summary);
        assertTrue(summary.getPreviousAppraisals().isEmpty());
        assertFalse(summary.getReviewContext().isPreviousReviewAvailable());
        assertNull(summary.getReviewContext().getCurrentReviewId());
    }

    @Test
    public void testEnterprisePerformanceScoreScaleDistinct() {
        rlsSessionBinder.bindCurrentTenant();
        // Create an enterprise performance cycle
        com.example.ems.performance.entity.PerformanceReviewCycle entCycle = new com.example.ems.performance.entity.PerformanceReviewCycle();
        entCycle.setOrganization(orgA);
        entCycle.setCode("ENT-2025-" + System.currentTimeMillis() % 10000);
        entCycle.setName("2025 Enterprise Performance Cycle");
        entCycle.setStartDate(LocalDate.of(2025, 1, 1));
        entCycle.setEndDate(LocalDate.of(2025, 12, 31));
        entCycle.setPeriodType("ANNUAL");
        entCycle.setStatus("COMPLETED");
        entCycle = performanceReviewCycleRepository.save(entCycle);

        // Create a published enterprise performance review record
        com.example.ems.performance.entity.PerformanceReviewRecord rec = new com.example.ems.performance.entity.PerformanceReviewRecord();
        rec.setOrganization(orgA);
        rec.setEmployee(empA);
        rec.setCycle(entCycle);
        rec.setStatus("PUBLISHED");
        rec.setCalculatedScore(BigDecimal.valueOf(92.00));
        rec.setFinalScore(BigDecimal.valueOf(92.00));
        rec.setRatingBand("OUTSTANDING");
        rec.setApprovedAt(LocalDateTime.of(2025, 12, 30, 15, 0));
        performanceReviewRecordRepository.save(rec);

        EmployeePerformanceSummaryDto summary = evaluationService.getEmployeePerformanceSummary(empA.getId());

        assertNotNull(summary);
        assertNotNull(summary.getPreviousAppraisals());
        assertEquals(1, summary.getPreviousAppraisals().size());

        EmployeePerformanceSummaryDto.PreviousAppraisalDto prev = summary.getPreviousAppraisals().get(0);
        assertEquals("2025 Enterprise Performance Cycle", prev.getCycle());
        assertEquals(92.00, prev.getFinalScore());
        assertEquals(100.0, prev.getMaxScore());
        assertEquals("100_POINT", prev.getScoreScale());
        assertEquals("OUTSTANDING", prev.getRating());
    }

    @Test
    public void testMultipleAppraisalsDescendingAndDraftExcluded() {
        // Create 2023 completed appraisal
        Appraisal a2023 = new Appraisal();
        a2023.setOrganization(orgA);
        a2023.setEmployee(empA);
        a2023.setStatus(AppraisalStatus.COMPLETED);
        a2023.setFinalRating(3.8);
        a2023.setCompletedAt(LocalDateTime.of(2023, 12, 20, 10, 0));
        appraisalRepository.save(a2023);

        // Create 2024 published appraisal
        Appraisal a2024 = new Appraisal();
        a2024.setOrganization(orgA);
        a2024.setEmployee(empA);
        a2024.setStatus(AppraisalStatus.PUBLISHED);
        a2024.setFinalRating(4.5);
        a2024.setCompletedAt(LocalDateTime.of(2024, 12, 22, 10, 0));
        appraisalRepository.save(a2024);

        // Create CANCELLED appraisal (must be excluded)
        Appraisal cancelled = new Appraisal();
        cancelled.setOrganization(orgA);
        cancelled.setEmployee(empA);
        cancelled.setStatus(AppraisalStatus.CANCELLED);
        cancelled.setCompletedAt(LocalDateTime.of(2025, 6, 1, 10, 0));
        appraisalRepository.save(cancelled);

        // Create DRAFT appraisal (must be excluded)
        Appraisal draft = new Appraisal();
        draft.setOrganization(orgA);
        draft.setEmployee(empA);
        draft.setStatus(AppraisalStatus.DRAFT);
        appraisalRepository.save(draft);

        EmployeePerformanceSummaryDto summary = evaluationService.getEmployeePerformanceSummary(empA.getId());

        assertNotNull(summary.getPreviousAppraisals());
        assertEquals(2, summary.getPreviousAppraisals().size());

        // Must be in descending order: 2024 then 2023
        assertEquals(4.5, summary.getPreviousAppraisals().get(0).getFinalScore());
        assertEquals(3.8, summary.getPreviousAppraisals().get(1).getFinalScore());

        // Review context should reflect the active draft
        assertEquals(draft.getId(), summary.getReviewContext().getCurrentReviewId());
    }

    @Test
    public void testCalculatedKpiReturnsActualScore() {
        rlsSessionBinder.bindCurrentTenant();

        // Create active cycle
        com.example.ems.performance.entity.PerformanceReviewCycle activeCycle = new com.example.ems.performance.entity.PerformanceReviewCycle();
        activeCycle.setOrganization(orgA);
        activeCycle.setCode("ACT-2026-" + System.currentTimeMillis() % 10000);
        activeCycle.setName("2026 KPI Active Cycle");
        activeCycle.setStartDate(LocalDate.of(2026, 1, 1));
        activeCycle.setEndDate(LocalDate.of(2026, 12, 31));
        activeCycle.setPeriodType("ANNUAL");
        activeCycle.setStatus("ACTIVE");
        activeCycle = performanceReviewCycleRepository.save(activeCycle);

        // Link an active appraisal
        AppraisalCycle appCycle = new AppraisalCycle();
        appCycle.setOrganization(orgA);
        appCycle.setName(activeCycle.getName());
        appCycle.setStartDate(activeCycle.getStartDate());
        appCycle.setEndDate(activeCycle.getEndDate());
        appCycle = cycleRepository.save(appCycle);

        Appraisal activeAppraisal = new Appraisal();
        activeAppraisal.setOrganization(orgA);
        activeAppraisal.setEmployee(empA);
        activeAppraisal.setCycle(appCycle);
        activeAppraisal.setStatus(AppraisalStatus.STAGE_REVIEW);
        appraisalRepository.save(activeAppraisal);

        // Create calculated PerformanceReviewRecord with KPI score 88.50
        com.example.ems.performance.entity.PerformanceReviewRecord rec = new com.example.ems.performance.entity.PerformanceReviewRecord();
        rec.setOrganization(orgA);
        rec.setEmployee(empA);
        rec.setCycle(activeCycle);
        rec.setStatus("CALCULATED");
        rec.setKpiWeightedScore(BigDecimal.valueOf(88.50));
        rec.setCalculatedScore(BigDecimal.valueOf(85.00));
        rec.setCalculatedAt(LocalDateTime.now());
        performanceReviewRecordRepository.save(rec);

        EmployeePerformanceSummaryDto summary = evaluationService.getEmployeePerformanceSummary(empA.getId());

        assertNotNull(summary.getCurrentPeriodPerformance());
        assertEquals(88.50, summary.getCurrentPeriodPerformance().getKpiAchievementPercentage());
    }

    @Test
    public void testZeroGoalsAndZeroAttendanceSafety() {
        EmployeePerformanceSummaryDto summary = evaluationService.getEmployeePerformanceSummary(empA.getId());

        assertNotNull(summary.getCurrentPeriodPerformance());
        assertEquals(0, summary.getCurrentPeriodPerformance().getTotalGoals());
        assertEquals(0, summary.getCurrentPeriodPerformance().getCompletedGoals());
        assertEquals(0.0, summary.getCurrentPeriodPerformance().getGoalCompletionPercentage());
        assertEquals(0.0, summary.getCurrentPeriodPerformance().getAttendancePercentage());
        assertEquals(0, summary.getCurrentPeriodPerformance().getPresentDays());
        assertTrue(summary.getCurrentPeriodPerformance().getWorkingDays() > 0);
    }

    @Test
    public void testLeaveSpanningPeriodBoundary() {
        // Active cycle 2026-01-01 to 2026-12-31
        AppraisalCycle cycle = new AppraisalCycle();
        cycle.setOrganization(orgA);
        cycle.setName("Cycle 2026");
        cycle.setStartDate(LocalDate.of(2026, 1, 1));
        cycle.setEndDate(LocalDate.of(2026, 12, 31));
        cycle = cycleRepository.save(cycle);

        Appraisal activeApp = new Appraisal();
        activeApp.setOrganization(orgA);
        activeApp.setEmployee(empA);
        activeApp.setCycle(cycle);
        activeApp.setStatus(AppraisalStatus.STAGE_REVIEW);
        appraisalRepository.save(activeApp);

        LeaveType leaveType = leaveTypeRepository.findByName("Casual Leave").orElseGet(() -> {
            LeaveType lt = new LeaveType();
            lt.setName("Casual Leave " + System.currentTimeMillis());
            lt.setOrganization(orgA);
            return leaveTypeRepository.save(lt);
        });

        // Leave from 2025-12-28 to 2026-01-04 (8 days total). Within 2026: Jan 1 to Jan 4 = 4 days.
        Leave leave = new Leave();
        leave.setOrganization(orgA);
        leave.setEmployee(empA);
        leave.setLeaveType(leaveType);
        leave.setStartDate(LocalDate.of(2025, 12, 28));
        leave.setEndDate(LocalDate.of(2026, 1, 4));
        leave.setDurationDays(8.0);
        leave.setStatus("APPROVED");
        leaveRepository.save(leave);

        EmployeePerformanceSummaryDto summary = evaluationService.getEmployeePerformanceSummary(empA.getId());

        assertNotNull(summary.getCurrentPeriodPerformance());
        // 4 clamped days out of 8 total = 4 days
        assertEquals(4, summary.getCurrentPeriodPerformance().getLeaveDays());
    }

    @Test
    public void testUnauthorizedUserForbidden() {
        // Regular employee user without APPRAISAL_VIEW querying another employee
        Role empRole = roleRepository.findByName("EMPLOYEE").orElseGet(() -> {
            Role r = new Role();
            r.setName("EMPLOYEE");
            return roleRepository.save(r);
        });

        User regUser = new User();
        regUser.setOrganization(orgA);
        regUser.setWorkEmail("unauth.emp@summaryorga.com");
        regUser.setRole(empRole);
        regUser = userRepository.save(regUser);

        String regToken = "Bearer " + jwtService.generateAccessToken(String.valueOf(regUser.getId()), regUser.getWorkEmail(), "EMPLOYEE");

        ResponseEntity<?> resp = evaluationController.getEmployeePerformanceSummary(regToken, empA.getId());
        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }
}
