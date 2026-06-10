package com.example.ems.controller;

import com.example.ems.dto.*;
import com.example.ems.entity.User;
import com.example.ems.repository.UserRepository;
import com.example.ems.service.AppraisalService;
import com.example.ems.service.JwtService;
import com.example.ems.service.RoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AppraisalControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock private AppraisalService appraisalService;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private RoleService roleService;

    @InjectMocks
    private AppraisalController appraisalController;

    private User hrUser;
    private User empUser;
    private final String hrEmail = "hr@example.com";
    private final String empEmail = "emp@example.com";
    private final String token = "mock-token";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(appraisalController).build();

        hrUser = new User();
        hrUser.setWorkEmail(hrEmail);
        hrUser.setEmployeeId("1");

        empUser = new User();
        empUser.setWorkEmail(empEmail);
        empUser.setEmployeeId("2");
    }

    private void setupManager() {
        when(jwtService.validateAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(hrEmail);
        when(userRepository.findByWorkEmail(hrEmail)).thenReturn(Optional.of(hrUser));
        when(roleService.hasPermission(hrEmail, "employee.update")).thenReturn(true);
    }

    private void setupEmployee() {
        when(jwtService.validateAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(empEmail);
        when(userRepository.findByWorkEmail(empEmail)).thenReturn(Optional.of(empUser));
        when(roleService.hasPermission(empEmail, "employee.update")).thenReturn(false);
        when(roleService.hasPermission(empEmail, "employee.delete")).thenReturn(false);
        when(roleService.hasPermission(empEmail, "recruitment.manage")).thenReturn(false);
    }

    // ── 1. DASHBOARD ──────────────────────────────────────────────────────────
    @Test
    public void testGetDashboardSuccess() throws Exception {
        setupManager();

        AppraisalDashboardResponse stats = new AppraisalDashboardResponse();
        stats.setTotalAppraisals(10);
        stats.setPendingSelfReviews(4);
        stats.setPendingManagerReviews(3);
        stats.setFinalizedAppraisals(3);
        stats.setAverageRating(4.0);

        when(appraisalService.getDashboardStats()).thenReturn(stats);

        mockMvc.perform(get("/api/appraisals/dashboard")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalAppraisals").value(10))
                .andExpect(jsonPath("$.data.averageRating").value(4.0));
    }

    @Test
    public void testGetDashboardForbidden() throws Exception {
        setupEmployee();

        mockMvc.perform(get("/api/appraisals/dashboard")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ── 2. APPRAISALS ─────────────────────────────────────────────────────────
    @Test
    public void testCreateAppraisalSuccess() throws Exception {
        setupManager();

        AppraisalRequest req = new AppraisalRequest();
        req.setEmployeeId(2L);
        req.setCycleId(1L);

        AppraisalResponse resp = new AppraisalResponse();
        resp.setId(10L);
        resp.setEmployeeId(2L);
        resp.setCycleId(1L);
        resp.setStatus("PENDING");

        when(appraisalService.createAppraisal(any(AppraisalRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/appraisals")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    public void testGetAppraisalsManager() throws Exception {
        setupManager();

        AppraisalResponse app = new AppraisalResponse();
        app.setId(10L);
        app.setEmployeeName("Jane Doe");

        when(appraisalService.getAppraisals()).thenReturn(List.of(app));

        mockMvc.perform(get("/api/appraisals")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(10));
    }

    @Test
    public void testGetAppraisalByIdSuccess() throws Exception {
        setupEmployee();

        AppraisalResponse app = new AppraisalResponse();
        app.setId(15L);
        app.setEmployeeId(2L); // matches empUser's employeeId
        app.setStatus("SELF_REVIEWED");

        when(appraisalService.getAppraisalById(15L)).thenReturn(Optional.of(app));

        mockMvc.perform(get("/api/appraisals/15")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SELF_REVIEWED"));
    }

    @Test
    public void testSubmitSelfReviewSuccess() throws Exception {
        setupEmployee();

        AppraisalSelfReviewRequest req = new AppraisalSelfReviewRequest();
        req.setSelfReview("Did really well");
        req.setSelfRating(4);

        AppraisalResponse oldApp = new AppraisalResponse();
        oldApp.setId(10L);
        oldApp.setEmployeeId(2L);

        AppraisalResponse newApp = new AppraisalResponse();
        newApp.setId(10L);
        newApp.setEmployeeId(2L);
        newApp.setStatus("SELF_REVIEWED");
        newApp.setSelfRating(4);

        when(appraisalService.getAppraisalById(10L)).thenReturn(Optional.of(oldApp));
        when(appraisalService.submitSelfReview(eq(10L), any(AppraisalSelfReviewRequest.class))).thenReturn(Optional.of(newApp));

        mockMvc.perform(post("/api/appraisals/10/self-review")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SELF_REVIEWED"));
    }

    @Test
    public void testSubmitManagerReviewSuccess() throws Exception {
        setupManager();

        AppraisalManagerReviewRequest req = new AppraisalManagerReviewRequest();
        req.setManagerReview("Excellent work");
        req.setManagerRating(5);

        AppraisalResponse updated = new AppraisalResponse();
        updated.setId(10L);
        updated.setStatus("MANAGER_REVIEWED");
        updated.setManagerRating(5);

        when(appraisalService.submitManagerReview(eq(10L), any(AppraisalManagerReviewRequest.class), eq(hrEmail))).thenReturn(Optional.of(updated));

        mockMvc.perform(post("/api/appraisals/10/manager-review")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("MANAGER_REVIEWED"));
    }

    @Test
    public void testFinalizeAppraisalSuccess() throws Exception {
        setupManager();

        AppraisalFinalizeRequest req = new AppraisalFinalizeRequest();
        req.setFinalRating(5);

        AppraisalResponse updated = new AppraisalResponse();
        updated.setId(10L);
        updated.setStatus("FINALIZED");
        updated.setFinalRating(5);

        when(appraisalService.finalizeAppraisal(eq(10L), any(AppraisalFinalizeRequest.class))).thenReturn(Optional.of(updated));

        mockMvc.perform(post("/api/appraisals/10/finalize")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("FINALIZED"));
    }

    // ── 3. INCREMENTS ─────────────────────────────────────────────────────────
    @Test
    public void testCreateIncrementSuccess() throws Exception {
        setupManager();

        IncrementRequest req = new IncrementRequest();
        req.setEmployeeId(2L);
        req.setIncrementPercentage(BigDecimal.valueOf(10.5));
        req.setEffectiveDate(LocalDate.of(2026, 7, 1));

        IncrementResponse resp = new IncrementResponse();
        resp.setId(20L);
        resp.setEmployeeId(2L);
        resp.setIncrementPercentage(BigDecimal.valueOf(10.5));
        resp.setStatus("PENDING");

        when(appraisalService.createIncrement(any(IncrementRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/increments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(20));
    }

    @Test
    public void testApproveIncrementSuccess() throws Exception {
        setupManager();

        IncrementResponse resp = new IncrementResponse();
        resp.setId(20L);
        resp.setStatus("APPROVED");

        when(appraisalService.approveIncrement(eq(20L), eq(hrEmail))).thenReturn(Optional.of(resp));

        mockMvc.perform(patch("/api/increments/20/approve")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    public void testApplyIncrementSuccess() throws Exception {
        setupManager();

        IncrementResponse resp = new IncrementResponse();
        resp.setId(20L);
        resp.setStatus("APPLIED");

        when(appraisalService.applyIncrement(eq(20L))).thenReturn(Optional.of(resp));

        mockMvc.perform(post("/api/increments/20/apply")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPLIED"));
    }

    @Test
    public void testGetSalaryRevisionsSuccess() throws Exception {
        setupEmployee();

        SalaryRevisionResponse rev = new SalaryRevisionResponse();
        rev.setId(5L);
        rev.setPreviousSalary(BigDecimal.valueOf(80000));
        rev.setNewSalary(BigDecimal.valueOf(88000));
        rev.setChangePercentage(BigDecimal.valueOf(10.0));

        when(appraisalService.getSalaryRevisions(2L)).thenReturn(List.of(rev));

        mockMvc.perform(get("/api/employees/2/salary-revisions")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(5));
    }

    // ── 4. REPORTS ────────────────────────────────────────────────────────────
    @Test
    public void testGetAppraisalsReportSuccess() throws Exception {
        setupManager();

        Map<String, Object> mockReport = Map.of("totalAppraisalsCount", 10, "averageFinalRating", 4.2);
        when(appraisalService.getAppraisalsReport("summary")).thenReturn(mockReport);

        mockMvc.perform(get("/api/appraisals/reports/summary")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalAppraisalsCount").value(10));
    }

    // ── 5. LETTER ─────────────────────────────────────────────────────────────
    @Test
    public void testGetIncrementLetterSuccess() throws Exception {
        setupManager();

        IncrementLetterResponse letter = new IncrementLetterResponse();
        letter.setEmployeeName("Jane Doe");
        letter.setLetterBody("SUBJECT: SALARY REVISION CONFIRMATION\n\nDear Jane Doe...");

        when(appraisalService.getIncrementLetter(20L)).thenReturn(letter);

        mockMvc.perform(get("/api/increments/20/letter")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeName").value("Jane Doe"));
    }
}
