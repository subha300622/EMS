package com.example.ems.settings.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.entity.MyEmployeeDocument;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.repository.MyEmployeeDocumentRepository;
import com.example.ems.security.service.JwtService;
import com.example.ems.settings.service.MySettingsService;
import com.example.ems.payroll.entity.Payslip;
import com.example.ems.payroll.repository.PayslipRepository;
import com.example.ems.payroll.service.MyPayslipService;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.service.AttendanceService;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.expense.entity.Expense;
import com.example.ems.expense.repository.ExpenseRepository;
import com.example.ems.performance.entity.Goal;
import com.example.ems.performance.entity.PerformanceReview;
import com.example.ems.performance.repository.GoalRepository;
import com.example.ems.performance.repository.PerformanceReviewRepository;
import com.example.ems.training.entity.TrainingEnrollment;
import com.example.ems.training.repository.TrainingEnrollmentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class MyDataExportControllerTest {

    private MockMvc mockMvc;

    @Mock private UserRepository userRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private JwtService jwtService;
    @Mock private RoleService roleService;
    @Mock private MySettingsService mySettingsService;
    @Mock private PayslipRepository payslipRepository;
    @Mock private MyPayslipService myPayslipService;
    @Mock private AttendanceService attendanceService;
    @Mock private LeaveRepository leaveRepository;
    @Mock private ExpenseRepository expenseRepository;
    @Mock private GoalRepository goalRepository;
    @Mock private PerformanceReviewRepository reviewRepository;
    @Mock private MyEmployeeDocumentRepository employeeDocumentRepository;
    @Mock private TrainingEnrollmentRepository trainingEnrollmentRepository;

    @InjectMocks
    private MyDataExportController myDataExportController;

    private User empUser;
    private Employee empProfile;
    private final String empEmail = "employee@company.com";
    private final String token = "mock-token";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(myDataExportController).build();

        empUser = new User();
        empUser.setId(1L);
        empUser.setWorkEmail(empEmail);
        empUser.setEmployeeId("EMP101");

        empProfile = new Employee();
        empProfile.setId(1L);
        empProfile.setEmployeeId("EMP101");
        empProfile.setFullName("John Doe");
        empProfile.setEmail(empEmail);
        empProfile.setDepartment("Engineering");
        empProfile.setDesignation("Software Engineer");
    }

    private void setupAuthorized(String permission) {
        when(jwtService.validateAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(empEmail);
        when(userRepository.findByWorkEmail(empEmail)).thenReturn(Optional.of(empUser));
        when(employeeRepository.findByEmail(empEmail)).thenReturn(Optional.of(empProfile));
        if (permission != null) {
            when(roleService.hasPermission(empEmail, permission)).thenReturn(true);
        }
    }

    private void setupForbidden(String permission) {
        when(jwtService.validateAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(empEmail);
        when(userRepository.findByWorkEmail(empEmail)).thenReturn(Optional.of(empUser));
        when(employeeRepository.findByEmail(empEmail)).thenReturn(Optional.of(empProfile));
        if (permission != null) {
            when(roleService.hasPermission(empEmail, permission)).thenReturn(false);
            when(roleService.isSuperAdmin(empEmail)).thenReturn(false);
        }
    }

    @Test
    public void testExportPayslipsSuccess() throws Exception {
        setupAuthorized("payslip.self.download");

        Payslip payslip = new Payslip();
        payslip.setId(1L);
        payslip.setPayslipNumber("PAY-001");

        when(payslipRepository.findByPayrollEmployeeId(1L)).thenReturn(List.of(payslip));
        when(myPayslipService.getPayslipPdf(empEmail, 1L)).thenReturn("dummy pdf".getBytes());

        mockMvc.perform(get("/api/v1/my-payslips/export")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"payslips-export.zip\""));
    }

    @Test
    public void testExportPayslipsForbidden() throws Exception {
        setupForbidden("payslip.self.download");

        mockMvc.perform(get("/api/v1/my-payslips/export")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testExportAttendanceSuccess() throws Exception {
        setupAuthorized(null);

        Attendance attendance = new Attendance();
        attendance.setId(1L);
        attendance.setDate(LocalDate.now());
        attendance.setStatus("PRESENT");

        when(attendanceService.getAttendanceByEmployeeId(1L)).thenReturn(List.of(attendance));

        mockMvc.perform(get("/api/v1/my-attendance/export")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"attendance-export.csv\""));
    }

    @Test
    public void testExportLeavesSuccess() throws Exception {
        setupAuthorized(null);

        Leave leave = new Leave();
        leave.setId(1L);
        leave.setStartDate(LocalDate.now());
        leave.setEndDate(LocalDate.now());
        leave.setStatus("APPROVED");

        when(leaveRepository.findByEmployeeId(1L)).thenReturn(List.of(leave));

        mockMvc.perform(get("/api/v1/my-leaves/export")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"leaves-export.csv\""));
    }

    @Test
    public void testExportExpensesSuccess() throws Exception {
        setupAuthorized("expense.self.read");

        Expense expense = new Expense();
        expense.setId(1L);
        expense.setAmount(new java.math.BigDecimal("100.00"));
        expense.setStatus("APPROVED");
        expense.setExpenseDate(LocalDate.now());

        when(expenseRepository.findByEmployeeId(1L)).thenReturn(List.of(expense));

        mockMvc.perform(get("/api/v1/my-expenses/export")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"expenses-export.csv\""));
    }

    @Test
    public void testExportPerformanceSuccess() throws Exception {
        setupAuthorized("performance.self.read");

        Goal goal = new Goal();
        goal.setId(1L);
        goal.setGoalCode("G-01");
        goal.setTitle("Achieve Target");
        goal.setGoalType("INDIVIDUAL");
        goal.setProgress(80);
        goal.setStatus("IN_PROGRESS");

        PerformanceReview review = new PerformanceReview();
        review.setId(1L);
        review.setReviewType("SELF");
        review.setStatus("FINALIZED");
        review.setRating(4);

        when(goalRepository.findByEmployeeId(1L)).thenReturn(List.of(goal));
        when(reviewRepository.findByEmployeeId(1L)).thenReturn(List.of(review));

        mockMvc.perform(get("/api/v1/my-performance/export")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"));
    }

    @Test
    public void testExportDocumentsSuccess() throws Exception {
        setupAuthorized("document.self.download");

        MyEmployeeDocument doc = new MyEmployeeDocument();
        doc.setId(1L);
        doc.setFileName("passport.pdf");
        doc.setFileData("dummy file data".getBytes());

        when(employeeDocumentRepository.findByEmployeeId(1L)).thenReturn(List.of(doc));

        mockMvc.perform(get("/api/v1/my-documents/export")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"));
    }

    @Test
    public void testExportTrainingsSuccess() throws Exception {
        setupAuthorized(null);

        TrainingEnrollment enrollment = new TrainingEnrollment();
        enrollment.setId(1L);
        enrollment.setStatus("COMPLETED");
        enrollment.setProgressPercent(100);
        enrollment.setGrade("A");

        when(trainingEnrollmentRepository.findByEmployeeId(1L)).thenReturn(List.of(enrollment));

        mockMvc.perform(get("/api/v1/my-trainings/export")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"));
    }

    @Test
    public void testSettingsDataExportSuccess() throws Exception {
        setupAuthorized("settings.data.export");

        when(mySettingsService.exportData(empEmail)).thenReturn(Map.of("requestId", "EXP-001", "status", "PROCESSING"));

        mockMvc.perform(post("/api/v1/my-settings/data/export")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.requestId").value("EXP-001"));
    }

    @Test
    public void testSettingsGetExportStatusSuccess() throws Exception {
        setupAuthorized("settings.data.export");

        when(mySettingsService.getExportStatus(empEmail, "EXP-001")).thenReturn(Map.of("requestId", "EXP-001", "status", "COMPLETED"));

        mockMvc.perform(get("/api/v1/my-settings/data/export/EXP-001")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    public void testSettingsDownloadExportedDataSuccess() throws Exception {
        setupAuthorized("settings.data.export");

        when(mySettingsService.getExportedDataCsv(empEmail, "EXP-001")).thenReturn("personal,data,csv".getBytes());

        mockMvc.perform(get("/api/v1/my-settings/data/export/EXP-001/download")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"));
    }
}
