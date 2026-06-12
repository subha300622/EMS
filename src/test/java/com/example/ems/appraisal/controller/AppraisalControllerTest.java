package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.AppraisalDashboardResponse;
import com.example.ems.appraisal.dto.AppraisalFinalizeRequest;
import com.example.ems.appraisal.dto.AppraisalManagerReviewRequest;
import com.example.ems.appraisal.dto.AppraisalRequest;
import com.example.ems.appraisal.dto.AppraisalResponse;
import com.example.ems.appraisal.dto.AppraisalSelfReviewRequest;
import com.example.ems.appraisal.dto.IncrementLetterResponse;
import com.example.ems.appraisal.dto.IncrementRequest;
import com.example.ems.appraisal.dto.IncrementResponse;
import com.example.ems.appraisal.dto.SalaryRevisionResponse;
import com.example.ems.appraisal.service.AppraisalService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.security.service.JwtService;

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

    private void setupFinance() {
        when(jwtService.validateAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn("finance@example.com");
        User financeUser = new User();
        financeUser.setWorkEmail("finance@example.com");
        financeUser.setEmployeeId("3");
        com.example.ems.auth.entity.Role financeRole = new com.example.ems.auth.entity.Role();
        financeRole.setName("FINANCE");
        financeUser.setRole(financeRole);
        financeUser.setRequestedRole("FINANCE");
        when(userRepository.findByWorkEmail("finance@example.com")).thenReturn(Optional.of(financeUser));
        when(roleService.hasPermission("finance@example.com", "employee.update")).thenReturn(false);
        when(roleService.hasPermission("finance@example.com", "employee.delete")).thenReturn(false);
        when(roleService.hasPermission("finance@example.com", "recruitment.manage")).thenReturn(false);
        when(roleService.hasPermission("finance@example.com", "salary.manage")).thenReturn(true);
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

        mockMvc.perform(get("/api/v1/appraisals/dashboard")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalAppraisals").value(10))
                .andExpect(jsonPath("$.data.averageRating").value(4.0));
    }

    @Test
    public void testGetDashboardForbidden() throws Exception {
        setupEmployee();

        mockMvc.perform(get("/api/v1/appraisals/dashboard")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetDashboardFinance() throws Exception {
        setupFinance();

        AppraisalDashboardResponse stats = new AppraisalDashboardResponse();
        stats.setTotalAppraisals(10);
        stats.setPendingSelfReviews(4);
        stats.setPendingManagerReviews(3);
        stats.setFinalizedAppraisals(3);
        stats.setAverageRating(4.0);

        when(appraisalService.getDashboardStats()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/appraisals/dashboard")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalAppraisals").value(10));
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

        mockMvc.perform(post("/api/v1/appraisals")
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

        mockMvc.perform(get("/api/v1/appraisals")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(10));
    }

    @Test
    public void testGetAppraisalsFinance() throws Exception {
        setupFinance();

        AppraisalResponse app = new AppraisalResponse();
        app.setId(10L);
        app.setEmployeeName("Jane Doe");

        when(appraisalService.getAppraisals()).thenReturn(List.of(app));

        mockMvc.perform(get("/api/v1/appraisals")
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

        mockMvc.perform(get("/api/v1/appraisals/15")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SELF_REVIEWED"));
    }

    @Test
    public void testGetAppraisalByIdFinance() throws Exception {
        setupFinance();

        AppraisalResponse app = new AppraisalResponse();
        app.setId(15L);
        app.setEmployeeId(2L); // doesn't match finance user employeeId (3) but finance has access
        app.setStatus("SELF_REVIEWED");

        when(appraisalService.getAppraisalById(15L)).thenReturn(Optional.of(app));

        mockMvc.perform(get("/api/v1/appraisals/15")
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

        mockMvc.perform(post("/api/v1/appraisals/10/self-review")
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

        mockMvc.perform(post("/api/v1/appraisals/10/manager-review")
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

        mockMvc.perform(post("/api/v1/appraisals/10/finalize")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("FINALIZED"));
    }

    @Test
    public void testUpdateAppraisalSuccess() throws Exception {
        setupManager();

        AppraisalRequest req = new AppraisalRequest();
        req.setEmployeeId(2L);
        req.setCycleId(1L);

        AppraisalResponse updated = new AppraisalResponse();
        updated.setId(10L);
        updated.setEmployeeId(2L);
        updated.setCycleId(1L);
        updated.setStatus("PENDING");

        when(appraisalService.updateAppraisal(eq(10L), any(AppraisalRequest.class))).thenReturn(Optional.of(updated));

        mockMvc.perform(put("/api/v1/appraisals/10")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10));
    }

    @Test
    public void testUpdateAppraisalNotFound() throws Exception {
        setupManager();

        AppraisalRequest req = new AppraisalRequest();
        req.setEmployeeId(2L);
        req.setCycleId(1L);

        when(appraisalService.updateAppraisal(eq(10L), any(AppraisalRequest.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/appraisals/10")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testDeleteAppraisalSuccess() throws Exception {
        setupManager();

        when(appraisalService.deleteAppraisal(10L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/appraisals/10")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testDeleteAppraisalNotFound() throws Exception {
        setupManager();

        when(appraisalService.deleteAppraisal(10L)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/appraisals/10")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── 4. REPORTS ────────────────────────────────────────────────────────────
    @Test
    public void testGetAppraisalsReportSuccess() throws Exception {
        setupManager();

        Map<String, Object> mockReport = Map.of("totalAppraisalsCount", 10, "averageFinalRating", 4.2);
        when(appraisalService.getAppraisalsReport("summary")).thenReturn(mockReport);

        mockMvc.perform(get("/api/v1/appraisals/reports/summary")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalAppraisalsCount").value(10));
    }
}
