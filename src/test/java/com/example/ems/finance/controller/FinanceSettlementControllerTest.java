package com.example.ems.finance.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.payroll.entity.FnfSettlement;
import com.example.ems.payroll.entity.FnfSettlementStatus;
import com.example.ems.payroll.entity.PaymentMode;
import com.example.ems.finance.dto.*;
import com.example.ems.finance.service.FinanceSettlementService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FinanceSettlementControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FinanceSettlementService settlementService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private FinanceSettlementController controller;

    private static final String TOKEN = "mock-finance-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "finance@company.com";
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);
        User user = new User();
        user.setWorkEmail(EMAIL);
        user.setFullName("Finance Manager");
        when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(user));
        when(roleService.hasRoleOrGreater(any(User.class), eq("FINANCE"))).thenReturn(true);
    }

    @Test
    public void testGetDashboard() throws Exception {
        SettlementDashboardResponse dashboard = new SettlementDashboardResponse(
                12L, 25L, 18L, 2L,
                BigDecimal.valueOf(4250000), BigDecimal.valueOf(152000), BigDecimal.valueOf(850000)
        );
        when(settlementService.getDashboard()).thenReturn(dashboard);

        mockMvc.perform(get("/api/v1/finance/settlements/dashboard")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pendingReview").value(12))
                .andExpect(jsonPath("$.data.totalSettlementAmount").value(4250000));
    }

    @Test
    public void testGetSettlementsList() throws Exception {
        SettlementListItem item = new SettlementListItem(
                1001L, 101L, "Ravi Kumar", "Marketing",
                LocalDate.of(2026, 5, 15),
                BigDecimal.valueOf(240000), BigDecimal.valueOf(15000), BigDecimal.valueOf(225000),
                "PENDING_REVIEW"
        );
        SettlementListResponse listResponse = new SettlementListResponse(List.of(item), 0, 10, 1, 1);
        when(settlementService.getSettlements(any(), any(), any(), eq(0), eq(10))).thenReturn(listResponse);

        mockMvc.perform(get("/api/v1/finance/settlements")
                        .header("Authorization", AUTH_HEADER)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].employeeName").value("Ravi Kumar"));
    }

    @Test
    public void testGetSettlementById() throws Exception {
        SettlementReviewResponse review = new SettlementReviewResponse(
                1001L,
                new SettlementReviewResponse.EmployeeInfo(101L, "Ravi Kumar", "Marketing"),
                List.of(new LineItem("Last Working Month Salary", BigDecimal.valueOf(120000))),
                List.of(new LineItem("Asset Recovery", BigDecimal.valueOf(10000))),
                BigDecimal.valueOf(240000), BigDecimal.valueOf(15000), BigDecimal.valueOf(225000),
                "PENDING_REVIEW"
        );
        when(settlementService.getReviewPopup(1001L)).thenReturn(review);

        mockMvc.perform(get("/api/v1/finance/settlements/1001")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.grossAmount").value(240000));
    }

    @Test
    public void testGetAssetClearance() throws Exception {
        AssetRecoveryResponse recovery = new AssetRecoveryResponse(
                101L, 3, 1, BigDecimal.valueOf(10000),
                List.of(new AssetRecoveryItem(5001L, "Dell Latitude", "NOT_RETURNED", BigDecimal.valueOf(10000)))
        );
        when(settlementService.getAssetClearance(1001L)).thenReturn(recovery);

        mockMvc.perform(get("/api/v1/finance/settlements/1001/asset-clearance")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.returnedAssets").value(3))
                .andExpect(jsonPath("$.data.assets[0].assetName").value("Dell Latitude"));
    }

    @Test
    public void testGetTimeline() throws Exception {
        SettlementTimelineItem item = new SettlementTimelineItem(
                "PENDING_REVIEW", "HR Admin", LocalDateTime.now(), "Created"
        );
        when(settlementService.getTimeline(1001L)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/finance/settlements/1001/timeline")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].updatedBy").value("HR Admin"));
    }

    @Test
    public void testSendBack() throws Exception {
        doNothing().when(settlementService).sendBack(eq(1001L), eq("mismatch"), any());

        SendBackRequest req = new SendBackRequest("mismatch");
        mockMvc.perform(patch("/api/v1/finance/settlements/1001/send-back")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Settlement sent back to HR"));
    }

    @Test
    public void testReject() throws Exception {
        doNothing().when(settlementService).reject(eq(1001L), eq("rejected"), any());

        RejectRequest req = new RejectRequest("rejected");
        mockMvc.perform(patch("/api/v1/finance/settlements/1001/reject")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Settlement rejected successfully"));
    }

    @Test
    public void testApprove() throws Exception {
        FnfSettlement approved = new FnfSettlement();
        approved.setId(1001L);
        approved.setStatus(FnfSettlementStatus.APPROVED);
        when(settlementService.approve(eq(1001L), eq("approved remarks"), any())).thenReturn(approved);

        ApproveRequest req = new ApproveRequest("approved remarks");
        mockMvc.perform(patch("/api/v1/finance/settlements/1001/approve")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    public void testProcess() throws Exception {
        FnfSettlement processed = new FnfSettlement();
        processed.setId(1001L);
        processed.setStatus(FnfSettlementStatus.PROCESSED);
        when(settlementService.process(eq(1001L), eq(PaymentMode.BANK_TRANSFER), eq("TXN123"), any())).thenReturn(processed);

        ProcessRequest req = new ProcessRequest(PaymentMode.BANK_TRANSFER, "TXN123");
        mockMvc.perform(post("/api/v1/finance/settlements/1001/process")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PROCESSED"));
    }

    @Test
    public void testGetStatus() throws Exception {
        SettlementStatusResponse statusResp = new SettlementStatusResponse(
                1001L, "APPROVED", true, "Finance Manager", LocalDateTime.now()
        );
        when(settlementService.getStatusDetails(1001L)).thenReturn(statusResp);

        mockMvc.perform(get("/api/v1/finance/settlements/1001/status")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.readyForDisbursement").value(true));
    }

    @Test
    public void testGetPdfMetadata() throws Exception {
        SettlementPdfResponse pdf = new SettlementPdfResponse("FNF-Ravi-Kumar.pdf", "/api/v1/files/FNF-Ravi-Kumar.pdf");
        when(settlementService.getPdfMetadata(1001L)).thenReturn(pdf);

        mockMvc.perform(get("/api/v1/finance/settlements/1001/pdf")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("FNF-Ravi-Kumar.pdf"));
    }

    @Test
    public void testGetReportsSummary() throws Exception {
        SettlementReportsSummaryResponse summary = new SettlementReportsSummaryResponse(
                120L, BigDecimal.valueOf(18500000), BigDecimal.valueOf(154166), 12L
        );
        when(settlementService.getReportsSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/v1/finance/settlements/reports/summary")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalProcessed").value(120));
    }

    @Test
    public void testExportMetadata() throws Exception {
        mockMvc.perform(get("/api/v1/finance/settlements/export")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("settlements-export.csv"));
    }
}
