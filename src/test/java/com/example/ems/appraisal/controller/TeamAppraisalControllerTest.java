package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.*;
import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.appraisal.entity.AppraisalCycle;
import com.example.ems.appraisal.entity.AppraisalStatus;
import com.example.ems.appraisal.repository.AppraisalCycleRepository;
import com.example.ems.appraisal.repository.AppraisalRepository;
import com.example.ems.appraisal.repository.IncrementRepository;
import com.example.ems.appraisal.entity.Increment;
import com.example.ems.appraisal.service.AppraisalService;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.ems.common.dto.ApiResponse;

@SpringBootTest
@Transactional
public class TeamAppraisalControllerTest {

    private ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AppraisalCycleRepository cycleRepository;

    @Autowired
    private AppraisalRepository appraisalRepository;

    @Autowired
    private AppraisalService appraisalService;

    @Autowired
    private IncrementRepository incrementRepository;

    private Employee managerEmp;
    private Employee directReportEmp;
    private Employee otherEmp;

    private AppraisalCycle cycle;
    private Appraisal directReportAppraisal;
    private Appraisal otherAppraisal;

    @BeforeEach
    public void setup() {
        // Resolve roles
        Role managerRole = roleRepository.findByName("MANAGER")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("MANAGER");
                    return roleRepository.save(r);
                });

        Role financeRole = roleRepository.findByName("FINANCE")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("FINANCE");
                    return roleRepository.save(r);
                });

        Role employeeRole = roleRepository.findByName("EMPLOYEE")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("EMPLOYEE");
                    return roleRepository.save(r);
                });

        // Set up manager
        User managerUser = userRepository.findByWorkEmail("test_manager@company.com")
                .orElseGet(() -> {
                    User u = new User();
                    u.setFullName("Test Manager");
                    u.setWorkEmail("test_manager@company.com");
                    u.setRole(managerRole);
                    u.setRequestedRole("MANAGER");
                    u.setPassword("password");
                    return userRepository.save(u);
                });
        managerEmp = employeeRepository.findByEmail("test_manager@company.com")
                .orElseGet(() -> {
                    Employee e = new Employee();
                    e.setFullName("Test Manager");
                    e.setEmail("test_manager@company.com");
                    e.setEmployeeId("EMP_MGR_001");
                    e.setDepartment("Engineering");
                    e.setDesignation("Engineering Manager");
                    e.setProfileImage("https://example.com/profiles/manager.png");
                    return employeeRepository.save(e);
                });
        managerUser.setUserId(managerEmp.getEmployeeId());
        userRepository.save(managerUser);

        // Set up finance user
        User financeUser = userRepository.findByWorkEmail("test_finance@company.com")
                .orElseGet(() -> {
                    User u = new User();
                    u.setFullName("Test Finance");
                    u.setWorkEmail("test_finance@company.com");
                    u.setRole(financeRole);
                    u.setRequestedRole("FINANCE");
                    u.setPassword("password");
                    return userRepository.save(u);
                });
        Employee financeEmp = employeeRepository.findByEmail("test_finance@company.com")
                .orElseGet(() -> {
                    Employee e = new Employee();
                    e.setFullName("Test Finance");
                    e.setEmail("test_finance@company.com");
                    e.setEmployeeId("EMP_FIN_001");
                    e.setDepartment("Finance");
                    e.setDesignation("Finance Specialist");
                    e.setProfileImage("https://example.com/profiles/finance.png");
                    return employeeRepository.save(e);
                });
        financeUser.setUserId(financeEmp.getEmployeeId());
        userRepository.save(financeUser);

        // Set up direct report
        User directReportUser = userRepository.findByWorkEmail("test_report@company.com")
                .orElseGet(() -> {
                    User u = new User();
                    u.setFullName("Test Direct Report");
                    u.setWorkEmail("test_report@company.com");
                    u.setRole(employeeRole);
                    u.setRequestedRole("EMPLOYEE");
                    u.setPassword("password");
                    return userRepository.save(u);
                });
        directReportEmp = employeeRepository.findByEmail("test_report@company.com")
                .orElseGet(() -> {
                    Employee e = new Employee();
                    e.setFullName("Test Direct Report");
                    e.setEmail("test_report@company.com");
                    e.setEmployeeId("EMP_REP_001");
                    e.setManager(managerEmp);
                    e.setAnnualSalary(java.math.BigDecimal.valueOf(100000.0));
                    e.setDepartment("Engineering");
                    e.setDesignation("Software Engineer");
                    e.setProfileImage("https://example.com/profiles/report.png");
                    return employeeRepository.save(e);
                });
        directReportUser.setUserId(directReportEmp.getEmployeeId());
        userRepository.save(directReportUser);

        // Set up other employee (not reporting to manager)
        User otherUser = userRepository.findByWorkEmail("test_other@company.com")
                .orElseGet(() -> {
                    User u = new User();
                    u.setFullName("Test Other Employee");
                    u.setWorkEmail("test_other@company.com");
                    u.setRole(employeeRole);
                    u.setRequestedRole("EMPLOYEE");
                    u.setPassword("password");
                    return userRepository.save(u);
                });
        otherEmp = employeeRepository.findByEmail("test_other@company.com")
                .orElseGet(() -> {
                    Employee e = new Employee();
                    e.setFullName("Test Other Employee");
                    e.setEmail("test_other@company.com");
                    e.setEmployeeId("EMP_OTH_001");
                    e.setAnnualSalary(java.math.BigDecimal.valueOf(120000.0));
                    e.setDepartment("Marketing");
                    e.setDesignation("Marketing Specialist");
                    e.setProfileImage("https://example.com/profiles/other.png");
                    return employeeRepository.save(e);
                });
        otherUser.setUserId(otherEmp.getEmployeeId());
        userRepository.save(otherUser);

        // Set up Appraisal Cycle
        cycle = cycleRepository.findByName("Test Cycle 2026")
                .orElseGet(() -> {
                    AppraisalCycle c = new AppraisalCycle();
                    c.setName("Test Cycle 2026");
                    c.setStartDate(LocalDate.of(2026, 1, 1));
                    c.setEndDate(LocalDate.of(2026, 12, 31));
                    c.setStatus("ACTIVE");
                    return cycleRepository.save(c);
                });

        // Set up Appraisals
        directReportAppraisal = new Appraisal();
        directReportAppraisal.setEmployee(directReportEmp);
        directReportAppraisal.setCycle(cycle);
        directReportAppraisal.setStatus(AppraisalStatus.ELIGIBLE);
        directReportAppraisal = appraisalRepository.save(directReportAppraisal);

        otherAppraisal = new Appraisal();
        otherAppraisal.setEmployee(otherEmp);
        otherAppraisal.setCycle(cycle);
        otherAppraisal.setStatus(AppraisalStatus.ELIGIBLE);
        otherAppraisal = appraisalRepository.save(otherAppraisal);
    }

    @Test
    public void testGetTeamSummary_Success() {
        TeamAppraisalSummaryDto summary = appraisalService.getTeamSummary("test_manager@company.com", cycle.getId());
        assertNotNull(summary);
        assertEquals(1, summary.getTotalCount());
        assertEquals(1, summary.getEligibleCount());
    }

    @Test
    public void testGetTeamList_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        PageResponse<TeamAppraisalListItemDto> result = appraisalService.getTeamAppraisals(
                "test_manager@company.com", cycle.getId(), null, null, pageable);
        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals("Test Direct Report", result.content().get(0).getEmployeeName());
    }

    @Test
    public void testGetAppraisalDetail_SuccessForDirectReport() {
        TeamAppraisalDetailDto detail = appraisalService.getAppraisalDetail("test_manager@company.com", directReportAppraisal.getId());
        assertNotNull(detail);
        assertEquals(directReportAppraisal.getId(), detail.getAppraisalId());
        assertEquals("Test Direct Report", detail.getEmployee().getName());
    }

    @Test
    public void testGetAppraisalDetail_ForbiddenForOtherEmployee() {
        assertThrows(IllegalArgumentException.class, () -> {
            appraisalService.getAppraisalDetail("test_manager@company.com", otherAppraisal.getId());
        });
    }

    @Test
    public void testSaveTeamRating_Success() {
        TeamAppraisalRatingRequest ratingReq = new TeamAppraisalRatingRequest(4.2, "Excellent performance and contribution");
        AppraisalResponse response = appraisalService.saveTeamRating("test_manager@company.com", directReportAppraisal.getId(), ratingReq);
        assertNotNull(response);
        assertEquals(4.2, response.getManagerRating());
        assertEquals("DRAFT", response.getStatus());
    }

    @Test
    public void testSubmitToFinance_Success() {
        // Set manager rating first to avoid null pointer/validation issues during calculation
        TeamAppraisalRatingRequest ratingReq = new TeamAppraisalRatingRequest(4.5, "Top contribution");
        appraisalService.saveTeamRating("test_manager@company.com", directReportAppraisal.getId(), ratingReq);

        AppraisalResponse response = appraisalService.submitToFinance("test_manager@company.com", directReportAppraisal.getId());
        assertNotNull(response);
        assertEquals("PENDING_FINANCE", response.getStatus());
    }

    @Test
    public void testFinanceApprove_Success() {
        // Set manager rating and submit to finance first
        TeamAppraisalRatingRequest ratingReq = new TeamAppraisalRatingRequest(4.0, "Great contribution");
        appraisalService.saveTeamRating("test_manager@company.com", directReportAppraisal.getId(), ratingReq);
        appraisalService.submitToFinance("test_manager@company.com", directReportAppraisal.getId());

        FinanceDecisionRequest decisionReq = new FinanceDecisionRequest("Approved as per standard guidelines");
        AppraisalResponse response = appraisalService.approveFinanceIncrement(directReportAppraisal.getId(), decisionReq, "test_finance@company.com");
        assertNotNull(response);
        assertEquals("FINANCE_APPROVED", response.getStatus());
        assertEquals(4.0, response.getFinalRating());
    }

    @Test
    public void testFinanceReject_Success() {
        // Set manager rating and submit to finance first
        TeamAppraisalRatingRequest ratingReq = new TeamAppraisalRatingRequest(4.0, "Great contribution");
        appraisalService.saveTeamRating("test_manager@company.com", directReportAppraisal.getId(), ratingReq);
        appraisalService.submitToFinance("test_manager@company.com", directReportAppraisal.getId());

        FinanceDecisionRequest decisionReq = new FinanceDecisionRequest("Exceeds budget limit");
        AppraisalResponse response = appraisalService.rejectFinanceIncrement(directReportAppraisal.getId(), decisionReq, "test_finance@company.com");
        assertNotNull(response);
        assertEquals("FINANCE_REJECTED", response.getStatus());
    }

    @Test
    public void testEndToEndAppraisalAndSalaryRevisionFlow_Success() throws Exception {
        // Step 1: CREATE APPRAISAL
        AppraisalRequest createReq = new AppraisalRequest();
        createReq.setEmployeeId(directReportEmp.getId());
        createReq.setCycleId(cycle.getId());
        AppraisalResponse appResponse = appraisalService.createAppraisal(createReq);
        assertNotNull(appResponse);
        Long appraisalId = appResponse.getId();
        assertEquals("ELIGIBLE", appResponse.getStatus());
        System.out.println("JSON_PAYLOAD_STEP_1: " + objectMapper.writeValueAsString(ApiResponse.success("Appraisal record created successfully", appResponse)));

        // Step 2: TEAM LIST VIEW (Manager Dashboard)
        Pageable pageable = PageRequest.of(0, 10);
        PageResponse<TeamAppraisalListItemDto> teamList = appraisalService.getTeamAppraisals(
                "test_manager@company.com", cycle.getId(), null, null, pageable);
        assertNotNull(teamList);
        assertTrue(teamList.content().stream().anyMatch(item -> item.getAppraisalId().equals(appraisalId)));
        System.out.println("JSON_PAYLOAD_STEP_2: " + objectMapper.writeValueAsString(ApiResponse.success("Team appraisals retrieved successfully", teamList)));

        // Step 3: MANAGER RATING
        TeamAppraisalRatingRequest ratingReq = new TeamAppraisalRatingRequest(4.5, "Outstanding performance and leadership");
        AppraisalResponse ratedApp = appraisalService.saveTeamRating("test_manager@company.com", appraisalId, ratingReq);
        assertNotNull(ratedApp);
        assertEquals(4.5, ratedApp.getManagerRating());
        assertEquals("DRAFT", ratedApp.getStatus());
        System.out.println("JSON_PAYLOAD_STEP_3: " + objectMapper.writeValueAsString(ApiResponse.success("Manager rating and comments saved successfully", ratedApp)));

        // Step 4: (OPTIONAL) ATTENDANCE JUSTIFICATION
        AttendanceJustificationRequest justifyReq = new AttendanceJustificationRequest("Medical leave justified by manager");
        AppraisalResponse justifiedApp = appraisalService.justifyAttendance("test_manager@company.com", appraisalId, justifyReq);
        assertNotNull(justifiedApp);
        Appraisal dbApp = appraisalRepository.findById(appraisalId).orElse(null);
        assertNotNull(dbApp);
        assertTrue(dbApp.isAttendanceJustified());
        assertEquals("PENDING_FINANCE", justifiedApp.getStatus());
        System.out.println("JSON_PAYLOAD_STEP_4: " + objectMapper.writeValueAsString(ApiResponse.success("Attendance justification submitted successfully", justifiedApp)));

        // Step 5 & 6: SUBMIT TO FINANCE -> STATUS -> PENDING_FINANCE
        // In the flow, manager submits to finance. If already justified, it continues as PENDING_FINANCE.
        AppraisalResponse submittedApp = appraisalService.submitToFinance("test_manager@company.com", appraisalId);
        assertNotNull(submittedApp);
        assertEquals("PENDING_FINANCE", submittedApp.getStatus());
        System.out.println("JSON_PAYLOAD_STEP_5_6: " + objectMapper.writeValueAsString(ApiResponse.success("Appraisal submitted to finance successfully", submittedApp)));

        // Step 7: FINANCE REVIEW (QUEUE SCREEN)
        PageResponse<TeamAppraisalListItemDto> financeQueue = appraisalService.getTeamAppraisals(
                "test_finance@company.com", cycle.getId(), null, AppraisalStatus.PENDING_FINANCE, pageable);
        assertNotNull(financeQueue);
        assertTrue(financeQueue.content().stream().anyMatch(item -> item.getAppraisalId().equals(appraisalId)));
        System.out.println("JSON_PAYLOAD_STEP_7: " + objectMapper.writeValueAsString(ApiResponse.success("Finance appraisal queue retrieved successfully", financeQueue)));

        // Step 8 & 9: FINANCE APPROVAL -> IF APPROVED -> FINANCE_APPROVED
        FinanceDecisionRequest financeDecision = new FinanceDecisionRequest("Budget approved for outstanding performer");
        AppraisalResponse approvedApp = appraisalService.approveFinanceIncrement(appraisalId, financeDecision, "test_finance@company.com");
        assertNotNull(approvedApp);
        assertEquals("FINANCE_APPROVED", approvedApp.getStatus());
        assertEquals(4.5, approvedApp.getFinalRating());
        System.out.println("JSON_PAYLOAD_STEP_8_9: " + objectMapper.writeValueAsString(ApiResponse.success("Appraisal increment approved successfully", approvedApp)));

        // Step 10: FINAL STEP -> PROCESSED (Payroll Execution)
        Increment increment = incrementRepository.findAll().stream()
                .filter(inc -> inc.getAppraisal() != null && inc.getAppraisal().getId().equals(appraisalId))
                .findFirst()
                .orElse(null);
        assertNotNull(increment);
        assertEquals("APPROVED", increment.getStatus());

        // Apply increment (execution of payroll increment)
        Optional<Increment> appliedIncrementOpt = appraisalService.applyIncrementEntity(increment.getId());
        assertTrue(appliedIncrementOpt.isPresent());
        assertEquals("APPLIED", appliedIncrementOpt.get().getStatus());

        // Assert that the Appraisal status has now transitioned to PROCESSED
        Appraisal finalAppraisal = appraisalRepository.findById(appraisalId).orElse(null);
        assertNotNull(finalAppraisal);
        assertEquals(AppraisalStatus.PROCESSED, finalAppraisal.getStatus());

        Increment appliedIncrement = appliedIncrementOpt.get();
        String revId = "REV" + String.format("%03d", appliedIncrement.getId());
        String appliedAtStr = appliedIncrement.getAppliedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")) + "Z";
        SalaryRevisionApplyResponse applyResp = new SalaryRevisionApplyResponse(
            revId,
            appliedIncrement.getEmployee() != null ? appliedIncrement.getEmployee().getEmployeeId() : null,
            appliedIncrement.getCurrentSalary(),
            appliedIncrement.getNewSalary(),
            appliedAtStr,
            appliedIncrement.getStatus() != null ? appliedIncrement.getStatus().toString() : null
        );
        ApiResponse<SalaryRevisionApplyResponse> apiResp10 = ApiResponse.success("Salary revision applied successfully", applyResp);
        System.out.println("JSON_PAYLOAD_STEP_10: " + objectMapper.writeValueAsString(apiResp10));
    }
}
