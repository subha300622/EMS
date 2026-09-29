package com.example.ems.attendance.scenario;

import com.example.ems.approval.service.ApprovalFacade;
import com.example.ems.attendance.dto.RegularizationApprovalRequest;
import com.example.ems.attendance.dto.adjustment.AdjustmentApprovalRequest;
import com.example.ems.attendance.dto.adjustment.AdjustmentResponseDto;
import com.example.ems.attendance.dto.adjustment.CreateAdjustmentRequest;
import com.example.ems.attendance.dto.late.LateAttendanceQuery;
import com.example.ems.attendance.dto.late.LateAttendanceReportDto;
import com.example.ems.attendance.dto.policy.AttendancePolicyDto;
import com.example.ems.attendance.dto.policy.CreateAttendancePolicyRequest;
import com.example.ems.attendance.entity.*;
import com.example.ems.attendance.exception.*;
import com.example.ems.attendance.repository.AttendanceAdjustmentRepository;
import com.example.ems.attendance.repository.AttendanceBreakRepository;
import com.example.ems.attendance.repository.AttendancePolicyRepository;
import com.example.ems.attendance.repository.AttendanceRegularizationRepository;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.attendance.service.*;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.entity.Team;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.repository.TeamMemberRepository;
import com.example.ems.employee.repository.TeamRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.dto.AuthPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive Scenario Testing Suite for the complete Attendance Architecture:
 * Policy Engine → Evaluator → Attendance Core → Late/Early Reporting → Correction → Regularization & Adjustment → Approval → Security
 */
@ExtendWith(MockitoExtension.class)
public class AttendanceScenarioIntegrationTest {

    // Repositories
    @Mock
    private AttendancePolicyRepository policyRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AttendanceBreakRepository attendanceBreakRepository;

    @Mock
    private AttendanceAdjustmentRepository adjustmentRepository;

    @Mock
    private AttendanceRegularizationRepository regularizationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private ApprovalFacade approvalFacade;

    // Services
    @Spy
    private AttendancePolicyEvaluator policyEvaluator = new AttendancePolicyEvaluator();

    @InjectMocks
    private AttendancePolicyService policyService;

    @InjectMocks
    private AttendanceCorrectionService correctionService;

    @InjectMocks
    private AttendanceAdjustmentService adjustmentService;

    @InjectMocks
    private AttendanceRegularizationService regularizationService;

    @InjectMocks
    private AttendanceLateEarlyService lateEarlyService;

    @InjectMocks
    private AttendanceService attendanceService;

    // Fixtures
    private Organization tenantA;
    private Organization tenantB;
    private Employee employeeA1;
    private Employee employeeA2;
    private Employee employeeB1;
    private AttendancePolicy standardPolicy;
    private ZoneId zone;

    @BeforeEach
    void setUp() {
        zone = ZoneId.of("Asia/Kolkata");

        // Wire dependencies manually where needed
        ReflectionTestUtils.setField(adjustmentService, "attendanceCorrectionService", correctionService);
        ReflectionTestUtils.setField(regularizationService, "attendanceService", attendanceService);
        ReflectionTestUtils.setField(attendanceService, "attendanceCorrectionService", correctionService);
        ReflectionTestUtils.setField(correctionService, "attendancePolicyService", policyService);
        ReflectionTestUtils.setField(correctionService, "attendancePolicyEvaluator", policyEvaluator);
        ReflectionTestUtils.setField(attendanceService, "attendancePolicyService", policyService);
        ReflectionTestUtils.setField(attendanceService, "attendancePolicyEvaluator", policyEvaluator);
        ReflectionTestUtils.setField(lateEarlyService, "attendancePolicyService", policyService);

        TenantContext.clear();
        TenantContext.setCurrentTenant(100L);

        tenantA = new Organization();
        tenantA.setId(100L);
        tenantA.setName("Tenant Alpha");

        tenantB = new Organization();
        tenantB.setId(200L);
        tenantB.setName("Tenant Beta");

        Department engineering = new Department();
        engineering.setId(1L);
        engineering.setName("Engineering");
        engineering.setOrganization(tenantA);

        Team teamBackend = new Team();
        teamBackend.setId(10L);
        teamBackend.setTeamName("Backend Team");
        teamBackend.setTeamCode("BE");
        teamBackend.setOrganization(tenantA);

        Team teamFrontend = new Team();
        teamFrontend.setId(20L);
        teamFrontend.setTeamName("Frontend Team");
        teamFrontend.setTeamCode("FE");
        teamFrontend.setOrganization(tenantA);

        employeeA1 = new Employee();
        employeeA1.setId(101L);
        employeeA1.setEmployeeId("EMP-101");
        employeeA1.setFullName("Alice Alpha");
        employeeA1.setEmail("alice@alphacorp.com");
        employeeA1.setOrganization(tenantA);
        employeeA1.setDepartment("Engineering");
        employeeA1.setStatus("ACTIVE");

        employeeA2 = new Employee();
        employeeA2.setId(102L);
        employeeA2.setEmployeeId("EMP-102");
        employeeA2.setFullName("Arthur Alpha");
        employeeA2.setEmail("arthur@alphacorp.com");
        employeeA2.setOrganization(tenantA);
        employeeA2.setDepartment("Engineering");
        employeeA2.setStatus("ACTIVE");

        employeeB1 = new Employee();
        employeeB1.setId(201L);
        employeeB1.setEmployeeId("EMP-201");
        employeeB1.setFullName("Bob Beta");
        employeeB1.setEmail("bob@betacorp.com");
        employeeB1.setOrganization(tenantB);
        employeeB1.setStatus("ACTIVE");

        standardPolicy = new AttendancePolicy();
        standardPolicy.setId(1L);
        standardPolicy.setOrganization(tenantA);
        standardPolicy.setName("Standard 9-to-6");
        standardPolicy.setOfficeStartTime(LocalTime.of(9, 0));
        standardPolicy.setOfficeEndTime(LocalTime.of(18, 0));
        standardPolicy.setGracePeriodMinutes(15);
        standardPolicy.setMinimumWorkingMinutes(480);
        standardPolicy.setHalfDayThreshold(240);
        standardPolicy.setLateThreshold(15);
        standardPolicy.setEarlyCheckoutThreshold(15);
        standardPolicy.setMaximumBreakMinutes(60);
        standardPolicy.setStatus(AttendancePolicyStatus.ACTIVE);

        // Security Context
        AuthPrincipal principal = new AuthPrincipal("101", "sess-101", 1, 100L, "alice@alphacorp.com", "ROLE_EMPLOYEE");
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.isAuthenticated()).thenReturn(true);
        lenient().when(auth.getPrincipal()).thenReturn(principal);
        lenient().when(auth.getName()).thenReturn("alice@alphacorp.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    // =========================================================================
    // 1. POLICY SCENARIOS
    // =========================================================================

    @Test
    @DisplayName("Scenario 1.1: Create policy starts in DRAFT status")
    void testScenario_CreatePolicy_StartsInDraft() {
        CreateAttendancePolicyRequest request = new CreateAttendancePolicyRequest();
        request.setName("Morning Shift");
        request.setOfficeStartTime(LocalTime.of(8, 0));
        request.setOfficeEndTime(LocalTime.of(17, 0));

        when(organizationRepository.findById(100L)).thenReturn(Optional.of(tenantA));
        when(policyRepository.save(any(AttendancePolicy.class))).thenAnswer(i -> {
            AttendancePolicy p = i.getArgument(0);
            p.setId(5L);
            return p;
        });

        AttendancePolicyDto created = policyService.createPolicy(request);
        assertNotNull(created);
        assertEquals(AttendancePolicyStatus.DRAFT, created.getStatus());
        assertEquals("Morning Shift", created.getName());
    }

    @Test
    @DisplayName("Scenario 1.2: Activate policy automatically deactivates previous active policies")
    void testScenario_ActivatePolicy_DeactivatesPreviousActive() {
        AttendancePolicy existingActive = new AttendancePolicy();
        existingActive.setId(1L);
        existingActive.setStatus(AttendancePolicyStatus.ACTIVE);

        AttendancePolicy draftPolicy = new AttendancePolicy();
        draftPolicy.setId(2L);
        draftPolicy.setStatus(AttendancePolicyStatus.DRAFT);

        when(policyRepository.findByIdAndOrganizationId(2L, 100L)).thenReturn(Optional.of(draftPolicy));
        when(policyRepository.findActivePoliciesForOrganization(100L)).thenReturn(List.of(existingActive));
        when(policyRepository.save(any(AttendancePolicy.class))).thenAnswer(i -> i.getArgument(0));

        AttendancePolicyDto activated = policyService.activatePolicy(2L);

        assertEquals(AttendancePolicyStatus.ACTIVE, activated.getStatus());
        assertEquals(AttendancePolicyStatus.INACTIVE, existingActive.getStatus());
        verify(policyRepository, times(1)).save(existingActive);
        verify(policyRepository, times(1)).save(draftPolicy);
    }

    @Test
    @DisplayName("Scenario 1.3: Reject policy creation with invalid start and end times")
    void testScenario_InvalidPolicyTimes_ThrowsException() {
        CreateAttendancePolicyRequest request = new CreateAttendancePolicyRequest();
        request.setName("Invalid Policy");
        request.setOfficeStartTime(LocalTime.of(18, 0));
        request.setOfficeEndTime(LocalTime.of(9, 0)); // End before start

        when(organizationRepository.findById(100L)).thenReturn(Optional.of(tenantA));

        assertThrows(IllegalArgumentException.class, () -> policyService.createPolicy(request));
    }

    // =========================================================================
    // 2. CHECK-IN SCENARIOS
    // =========================================================================

    @Test
    @DisplayName("Scenario 2: Evaluates exact check-in boundaries against 09:00 start + 15 min grace")
    void testScenario_CheckInBoundaries() {
        LocalDate date = LocalDate.of(2026, 9, 10);

        // 08:55 -> Not late
        Instant t0855 = date.atTime(8, 55).atZone(zone).toInstant();
        AttendancePolicyEvaluator.CheckInEvaluation r0855 = policyEvaluator.evaluateCheckIn(t0855, standardPolicy, zone);
        assertFalse(r0855.isLate());
        assertEquals(0, r0855.lateByMinutes());

        // 09:00 -> Not late
        Instant t0900 = date.atTime(9, 0).atZone(zone).toInstant();
        AttendancePolicyEvaluator.CheckInEvaluation r0900 = policyEvaluator.evaluateCheckIn(t0900, standardPolicy, zone);
        assertFalse(r0900.isLate());
        assertEquals(0, r0900.lateByMinutes());

        // 09:15 -> Not late (exact grace boundary)
        Instant t0915 = date.atTime(9, 15).atZone(zone).toInstant();
        AttendancePolicyEvaluator.CheckInEvaluation r0915 = policyEvaluator.evaluateCheckIn(t0915, standardPolicy, zone);
        assertFalse(r0915.isLate());
        assertEquals(0, r0915.lateByMinutes());

        // 09:16 -> Late by 16 minutes from work start time
        Instant t0916 = date.atTime(9, 16).atZone(zone).toInstant();
        AttendancePolicyEvaluator.CheckInEvaluation r0916 = policyEvaluator.evaluateCheckIn(t0916, standardPolicy, zone);
        assertTrue(r0916.isLate());
        assertEquals(16, r0916.lateByMinutes());

        // 10:00 -> Late by 60 minutes
        Instant t1000 = date.atTime(10, 0).atZone(zone).toInstant();
        AttendancePolicyEvaluator.CheckInEvaluation r1000 = policyEvaluator.evaluateCheckIn(t1000, standardPolicy, zone);
        assertTrue(r1000.isLate());
        assertEquals(60, r1000.lateByMinutes());
    }

    // =========================================================================
    // 3. CHECK-OUT SCENARIOS
    // =========================================================================

    @Test
    @DisplayName("Scenario 3: Evaluates checkout thresholds (18:00 end, 15m early threshold)")
    void testScenario_CheckOutBoundaries() {
        LocalDate date = LocalDate.of(2026, 9, 10);
        Instant checkIn = date.atTime(9, 0).atZone(zone).toInstant();

        // 17:44 -> Early checkout (before 17:45 threshold boundary)
        Instant t1744 = date.atTime(17, 44).atZone(zone).toInstant();
        AttendancePolicyEvaluator.CheckOutEvaluation r1744 = policyEvaluator.evaluateCheckOut(checkIn, t1744, 0, standardPolicy, zone);
        assertTrue(r1744.isEarlyCheckout());
        assertEquals(16, r1744.earlyByMinutes());

        // 17:45 -> Threshold boundary (not flagged as early checkout)
        Instant t1745 = date.atTime(17, 45).atZone(zone).toInstant();
        AttendancePolicyEvaluator.CheckOutEvaluation r1745 = policyEvaluator.evaluateCheckOut(checkIn, t1745, 0, standardPolicy, zone);
        assertFalse(r1745.isEarlyCheckout());
        assertEquals(0, r1745.earlyByMinutes());

        // 17:59 -> Within threshold (not early)
        Instant t1759 = date.atTime(17, 59).atZone(zone).toInstant();
        AttendancePolicyEvaluator.CheckOutEvaluation r1759 = policyEvaluator.evaluateCheckOut(checkIn, t1759, 0, standardPolicy, zone);
        assertFalse(r1759.isEarlyCheckout());

        // 18:00 -> Full on-time day (540 mins)
        Instant t1800 = date.atTime(18, 0).atZone(zone).toInstant();
        AttendancePolicyEvaluator.CheckOutEvaluation r1800 = policyEvaluator.evaluateCheckOut(checkIn, t1800, 0, standardPolicy, zone);
        assertFalse(r1800.isEarlyCheckout());
        assertFalse(r1800.isHalfDay());
        assertEquals(540, r1800.totalWorkingMinutes());

        // 18:30 -> Overtime / Full day
        Instant t1830 = date.atTime(18, 30).atZone(zone).toInstant();
        AttendancePolicyEvaluator.CheckOutEvaluation r1830 = policyEvaluator.evaluateCheckOut(checkIn, t1830, 0, standardPolicy, zone);
        assertFalse(r1830.isEarlyCheckout());
        assertEquals(570, r1830.totalWorkingMinutes());
    }

    // =========================================================================
    // 4. BREAK SCENARIOS
    // =========================================================================

    @Test
    @DisplayName("Scenario 4: Break validations & total working duration deduction")
    void testScenario_BreakCalculations() {
        LocalDate date = LocalDate.of(2026, 9, 10);
        Instant checkIn = date.atTime(9, 0).atZone(zone).toInstant();
        Instant checkOut = date.atTime(18, 0).atZone(zone).toInstant();

        // 60 minutes total break taken during 9-hour day -> 480 working minutes
        AttendancePolicyEvaluator.CheckOutEvaluation result = policyEvaluator.evaluateCheckOut(checkIn, checkOut, 60, standardPolicy, zone);
        assertEquals(480, result.totalWorkingMinutes());
        assertFalse(result.isHalfDay());
    }

    // =========================================================================
    // 5. LATE/EARLY REPORT SCENARIOS & UNSAFE SORT REJECTION
    // =========================================================================

    @Test
    @DisplayName("Scenario 5.1: Late report filters correctly for current tenant")
    void testScenario_LateReport_Filtered() {
        Attendance lateAtt = new Attendance();
        lateAtt.setId(301L);
        lateAtt.setEmployee(employeeA1);
        lateAtt.setOrganization(tenantA);
        lateAtt.setDate(LocalDate.of(2026, 9, 10));
        lateAtt.setCheckInTime(Instant.parse("2026-09-10T04:00:00Z")); // 09:30 IST
        lateAtt.setIsLate(true);
        lateAtt.setLateByMinutes(30);
        lateAtt.setStatus(AttendanceStatus.PRESENT);

        LateAttendanceQuery query = new LateAttendanceQuery();
        query.setFromDate(LocalDate.of(2026, 9, 1));
        query.setToDate(LocalDate.of(2026, 9, 10));

        when(policyRepository.findActivePoliciesForOrganization(100L)).thenReturn(List.of(standardPolicy));
        when(attendanceRepository.findLateAttendance(eq(100L), isNull(), isNull(), isNull(), eq(LocalDate.of(2026, 9, 1)), eq(LocalDate.of(2026, 9, 10)), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(lateAtt)));

        Page<LateAttendanceReportDto> report = lateEarlyService.getLateAttendanceReport(query);
        assertNotNull(report);
        assertEquals(1, report.getTotalElements());
        assertEquals("Alice Alpha", report.getContent().get(0).getEmployeeName());
        assertEquals(Integer.valueOf(30), report.getContent().get(0).getLateByMinutes());
    }

    @Test
    @DisplayName("Scenario 5.2: Rejects unsafe sort columns to prevent SQL injection")
    void testScenario_UnsafeSortField_ThrowsException() {
        LateAttendanceQuery query = new LateAttendanceQuery();
        query.setSortBy("password; DROP TABLE users;");

        assertThrows(IllegalArgumentException.class, () -> query.toPageable());
    }

    // =========================================================================
    // 6. REGULARIZATION SCENARIOS
    // =========================================================================

    @Test
    @DisplayName("Scenario 6: Regularization approval delegates to AttendanceCorrectionService")
    void testScenario_RegularizationApproval_AppliesCorrection() {
        Attendance attendance = new Attendance();
        attendance.setId(401L);
        attendance.setOrganization(tenantA);
        attendance.setEmployee(employeeA1);
        attendance.setDate(LocalDate.of(2026, 9, 10));
        attendance.setCheckInTime(Instant.parse("2026-09-10T04:15:00Z")); // 09:45 IST (Late)
        attendance.setCheckOutTime(Instant.parse("2026-09-10T12:30:00Z")); // 18:00 IST
        attendance.setStatus(AttendanceStatus.PRESENT);
        attendance.setIsLate(true);
        attendance.setLateByMinutes(45);

        AttendanceRegularization reg = new AttendanceRegularization();
        reg.setId(501L);
        reg.setOrganization(tenantA);
        reg.setEmployee(employeeA1);
        reg.setAttendance(attendance);
        reg.setRequestedCheckInTime(Instant.parse("2026-09-10T03:30:00Z")); // Corrected to 09:00 IST
        reg.setRequestedCheckOutTime(Instant.parse("2026-09-10T12:30:00Z")); // 18:00 IST
        reg.setStatus(AttendanceRegularizationStatus.PENDING);

        when(regularizationRepository.findByIdAndOrganizationId(501L, 100L))
                .thenReturn(Optional.of(reg));
        when(regularizationRepository.save(any(AttendanceRegularization.class))).thenAnswer(i -> i.getArgument(0));
        when(attendanceRepository.findByIdAndOrganizationId(401L, 100L)).thenReturn(Optional.of(attendance));
        when(policyRepository.findActivePoliciesForOrganization(100L)).thenReturn(List.of(standardPolicy));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> i.getArgument(0));

        regularizationService.approveRegularization(501L, new RegularizationApprovalRequest("Regularization approved"));

        assertEquals(AttendanceRegularizationStatus.APPROVED, reg.getStatus());
        // Verify attendance record was recalculated
        assertFalse(attendance.getIsLate());
        assertEquals(0, attendance.getLateByMinutes());
        assertEquals(540, attendance.getTotalWorkingMinutes());
    }

    // =========================================================================
    // 7. ADJUSTMENT SCENARIOS
    // =========================================================================

    @Test
    @DisplayName("Scenario 7: Adjustment approval applies corrections to attendance timestamps & metrics")
    void testScenario_AdjustmentApproval_AppliesCorrection() {
        Attendance attendance = new Attendance();
        attendance.setId(601L);
        attendance.setOrganization(tenantA);
        attendance.setEmployee(employeeA1);
        attendance.setDate(LocalDate.of(2026, 9, 10));
        attendance.setCheckInTime(Instant.parse("2026-09-10T04:15:00Z")); // 09:45 IST (Late)
        attendance.setCheckOutTime(Instant.parse("2026-09-10T11:00:00Z")); // 16:30 IST (Early)
        attendance.setIsLate(true);
        attendance.setLateByMinutes(45);
        attendance.setIsEarlyCheckout(true);
        attendance.setEarlyByMinutes(90);

        AttendanceAdjustment adj = new AttendanceAdjustment();
        adj.setId(701L);
        adj.setOrganization(tenantA);
        adj.setEmployee(employeeA1);
        adj.setAttendance(attendance);
        adj.setRequestedCheckInTime(Instant.parse("2026-09-10T03:30:00Z")); // 09:00 IST
        adj.setRequestedCheckOutTime(Instant.parse("2026-09-10T12:30:00Z")); // 18:00 IST
        adj.setStatus(AttendanceAdjustmentStatus.PENDING);

        when(adjustmentRepository.findByIdAndOrganizationId(701L, 100L)).thenReturn(Optional.of(adj));
        when(adjustmentRepository.save(any(AttendanceAdjustment.class))).thenAnswer(i -> i.getArgument(0));
        when(policyRepository.findActivePoliciesForOrganization(100L)).thenReturn(List.of(standardPolicy));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> i.getArgument(0));

        AdjustmentResponseDto result = adjustmentService.approveAdjustment(701L, new AdjustmentApprovalRequest("Approved by Admin"));

        assertNotNull(result);
        assertEquals(AttendanceAdjustmentStatus.APPROVED, result.getStatus());
        // Verify that attendance is fully corrected
        assertFalse(attendance.getIsLate());
        assertEquals(0, attendance.getLateByMinutes());
        assertFalse(attendance.getIsEarlyCheckout());
        assertEquals(0, attendance.getEarlyByMinutes());
        assertEquals(540, attendance.getTotalWorkingMinutes());
    }

    // =========================================================================
    // 8. CORRECTION CONSISTENCY (NO STALE VALUES)
    // =========================================================================

    @Test
    @DisplayName("Scenario 8: Complete recalculation clears stale late/early/half-day flags")
    void testScenario_CorrectionConsistency_NoStaleValues() {
        Attendance attendance = new Attendance();
        attendance.setId(801L);
        attendance.setOrganization(tenantA);
        attendance.setDate(LocalDate.of(2026, 9, 10));
        attendance.setCheckInTime(Instant.parse("2026-09-10T04:30:00Z")); // 10:00 IST (Late)
        attendance.setCheckOutTime(Instant.parse("2026-09-10T07:30:00Z")); // 13:00 IST (Half Day, Early)
        attendance.setIsLate(true);
        attendance.setLateByMinutes(60);
        attendance.setIsEarlyCheckout(true);
        attendance.setEarlyByMinutes(300);
        attendance.setIsHalfDay(true);
        attendance.setStatus(AttendanceStatus.HALF_DAY);
        attendance.setTotalWorkingMinutes(180);

        when(policyRepository.findActivePoliciesForOrganization(100L)).thenReturn(List.of(standardPolicy));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> i.getArgument(0));

        // Apply correction to standard 09:00 - 18:00
        Instant newCheckIn = LocalDate.of(2026, 9, 10).atTime(9, 0).atZone(zone).toInstant();
        Instant newCheckOut = LocalDate.of(2026, 9, 10).atTime(18, 0).atZone(zone).toInstant();

        Attendance corrected = correctionService.applyCorrection(
                attendance,
                newCheckIn,
                newCheckOut,
                "Regular shift correction",
                "ADMIN",
                "TEST_CORRECTION"
        );

        assertNotNull(corrected);
        assertFalse(corrected.getIsLate(), "isLate must be reset to false");
        assertEquals(0, corrected.getLateByMinutes(), "lateByMinutes must be reset to 0");
        assertFalse(corrected.getIsEarlyCheckout(), "isEarlyCheckout must be reset to false");
        assertEquals(0, corrected.getEarlyByMinutes(), "earlyByMinutes must be reset to 0");
        assertFalse(corrected.getIsHalfDay(), "isHalfDay must be reset to false");
        assertEquals(540, corrected.getTotalWorkingMinutes(), "workingMinutes must be recalculated to 540");
        assertEquals("COMPLETED", corrected.getStatus(), "status must be COMPLETED");
    }

    // =========================================================================
    // 9. TENANT-SECURITY ATTACK SCENARIOS
    // =========================================================================

    @Test
    @DisplayName("Scenario 9.1: Tenant A caller cannot read Tenant B policy")
    void testScenario_Security_CrossTenantPolicyAccess_Fails() {
        when(policyRepository.findByIdAndOrganizationId(999L, 100L)).thenReturn(Optional.empty());

        assertThrows(AttendanceNotFoundException.class, () -> policyService.getPolicyById(999L));
    }

    @Test
    @DisplayName("Scenario 9.2: Tenant A caller cannot create adjustment for Tenant B attendance")
    void testScenario_Security_CrossTenantAdjustment_Fails() {
        CreateAdjustmentRequest request = new CreateAdjustmentRequest(
                Instant.now(),
                Instant.now().plusSeconds(3600),
                "Cross tenant attempt"
        );

        when(attendanceRepository.findByIdAndOrganizationId(999L, 100L)).thenReturn(Optional.empty());

        assertThrows(AttendanceNotFoundException.class, () -> adjustmentService.createAdjustment(999L, request));
    }

    // =========================================================================
    // 10. END-TO-END FLOW SCENARIO
    // =========================================================================

    @Test
    @DisplayName("Scenario 10: Complete flow (Late checkin -> Late report -> Adjustment -> Approval -> Correction -> Clean report)")
    void testScenario_CompleteEndToEndFlow() {
        // Step 1: Employee checks in late at 09:45
        LocalDate date = LocalDate.of(2026, 9, 10);
        Instant lateCheckIn = date.atTime(9, 45).atZone(zone).toInstant();
        Instant normalCheckOut = date.atTime(18, 0).atZone(zone).toInstant();

        AttendancePolicyEvaluator.CheckInEvaluation lateEval = policyEvaluator.evaluateCheckIn(lateCheckIn, standardPolicy, zone);
        assertTrue(lateEval.isLate());
        assertEquals(45, lateEval.lateByMinutes());

        Attendance record = new Attendance();
        record.setId(1001L);
        record.setOrganization(tenantA);
        record.setEmployee(employeeA1);
        record.setDate(date);
        record.setCheckInTime(lateCheckIn);
        record.setCheckOutTime(normalCheckOut);
        record.setIsLate(true);
        record.setLateByMinutes(45);
        record.setStatus(AttendanceStatus.PRESENT);

        // Step 2: Query late report -> contains record
        when(policyRepository.findActivePoliciesForOrganization(100L)).thenReturn(List.of(standardPolicy));
        when(attendanceRepository.findLateAttendance(eq(100L), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(record)));

        Page<LateAttendanceReportDto> lateReportBefore = lateEarlyService.getLateAttendanceReport(new LateAttendanceQuery());
        assertEquals(1, lateReportBefore.getTotalElements());
        assertEquals(Integer.valueOf(45), lateReportBefore.getContent().get(0).getLateByMinutes());

        // Step 3: Employee creates Adjustment Request for 09:00 - 18:00
        Instant onTimeCheckIn = date.atTime(9, 0).atZone(zone).toInstant();
        CreateAdjustmentRequest adjReq = new CreateAdjustmentRequest(onTimeCheckIn, normalCheckOut, "Traffic delay, worked from car hotspot");

        when(attendanceRepository.findByIdAndOrganizationId(1001L, 100L)).thenReturn(Optional.of(record));
        when(adjustmentRepository.existsByAttendanceIdAndStatus(1001L, AttendanceAdjustmentStatus.PENDING)).thenReturn(false);
        when(adjustmentRepository.save(any(AttendanceAdjustment.class))).thenAnswer(i -> {
            AttendanceAdjustment a = i.getArgument(0);
            a.setId(2001L);
            return a;
        });

        AdjustmentResponseDto createdAdj = adjustmentService.createAdjustment(1001L, adjReq);
        assertEquals(AttendanceAdjustmentStatus.PENDING, createdAdj.getStatus());

        // Step 4: Manager Approves Adjustment -> applies correction
        when(adjustmentRepository.findByIdAndOrganizationId(2001L, 100L)).thenReturn(Optional.of(new AttendanceAdjustment() {{
            setId(2001L);
            setOrganization(tenantA);
            setEmployee(employeeA1);
            setAttendance(record);
            setRequestedCheckInTime(onTimeCheckIn);
            setRequestedCheckOutTime(normalCheckOut);
            setStatus(AttendanceAdjustmentStatus.PENDING);
        }}));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> i.getArgument(0));

        AdjustmentResponseDto approvedAdj = adjustmentService.approveAdjustment(2001L, new AdjustmentApprovalRequest("Approved"));
        assertEquals(AttendanceAdjustmentStatus.APPROVED, approvedAdj.getStatus());

        // Step 5: Verify record is completely corrected
        assertFalse(record.getIsLate());
        assertEquals(0, record.getLateByMinutes());
        assertEquals(540, record.getTotalWorkingMinutes());

        // Step 6: Query late report again -> now returns empty
        when(attendanceRepository.findLateAttendance(eq(100L), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<LateAttendanceReportDto> lateReportAfter = lateEarlyService.getLateAttendanceReport(new LateAttendanceQuery());
        assertEquals(0, lateReportAfter.getTotalElements());
    }
}
