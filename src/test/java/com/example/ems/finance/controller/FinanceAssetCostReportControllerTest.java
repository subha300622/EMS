package com.example.ems.finance.controller;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.finance.dto.*;
import com.example.ems.finance.service.FinanceAssetCostReportService;
import com.example.ems.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FinanceAssetCostReportControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private FinanceAssetCostReportService reportService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private FinanceAssetCostReportController reportController;

    @InjectMocks
    private FileDownloadController fileDownloadController;

    private User financeUser;
    private String token = "Bearer mock-token";
    private String email = "finance@example.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        
        // Setup standalone controller testing with both controllers
        mockMvc = MockMvcBuilders.standaloneSetup(reportController, fileDownloadController).build();

        financeUser = new User();
        financeUser.setWorkEmail(email);
        Role role = new Role();
        role.setName("FINANCE");
        financeUser.setRole(role);
    }

    private void mockAuthSuccess() {
        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(financeUser));
        when(roleService.hasRoleOrGreater(any(User.class), any(String.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            String r = invocation.getArgument(1);
            if (u == null || u.getRole() == null || r == null) return false;
            Map<String, Integer> hierarchy = Map.of(
                "SUPER_ADMIN", 1,
                "ADMIN", 2,
                "HR", 3,
                "MANAGER", 4,
                "FINANCE", 5,
                "EMPLOYEE", 6
            );
            Integer userLevel = hierarchy.get(u.getRole().getName());
            Integer targetLevel = hierarchy.get(r);
            if (userLevel == null || targetLevel == null) return false;
            return userLevel <= targetLevel;
        });
    }

    @Test
    public void testGetDashboardSuccess() throws Exception {
        mockAuthSuccess();
        AssetCostDashboardResponse responseObj = new AssetCostDashboardResponse(
                BigDecimal.valueOf(24000000),
                BigDecimal.valueOf(4200000),
                BigDecimal.valueOf(800000),
                14,
                315,
                LocalDate.of(2026, 6, 2)
        );
        when(reportService.getDashboard()).thenReturn(responseObj);

        mockMvc.perform(get("/api/v1/finance/asset-cost-report/dashboard")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalAssetValue").value(24000000))
                .andExpect(jsonPath("$.data.annualDepreciation").value(4200000))
                .andExpect(jsonPath("$.data.replacementDue").value(14))
                .andExpect(jsonPath("$.data.assetCount").value(315))
                .andExpect(jsonPath("$.data.asOfDate").value("2026-06-02"));
    }

    @Test
    public void testGetBreakdownSuccess() throws Exception {
        mockAuthSuccess();
        AssetCostBreakdownItem item = new AssetCostBreakdownItem(
                1L,
                "Accessories",
                95,
                BigDecimal.valueOf(900000),
                BigDecimal.valueOf(180000),
                BigDecimal.valueOf(720000),
                "ACTIVE"
        );
        Map<String, Object> responseMap = Map.of(
                "content", List.of(item),
                "page", 0,
                "size", 10,
                "totalElements", 1,
                "totalPages", 1
        );
        when(reportService.getBreakdown(any())).thenReturn(responseMap);

        mockMvc.perform(get("/api/v1/finance/asset-cost-report")
                .header("Authorization", token)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].categoryName").value("Accessories"))
                .andExpect(jsonPath("$.data.content[0].totalValue").value(900000))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    public void testGetCategoryDetailsSuccess() throws Exception {
        mockAuthSuccess();
        CategoryCostDetailsResponse details = new CategoryCostDetailsResponse(
                1L,
                "Accessories",
                95,
                BigDecimal.valueOf(900000),
                BigDecimal.valueOf(180000),
                BigDecimal.valueOf(720000),
                "ACTIVE",
                BigDecimal.valueOf(9474),
                BigDecimal.valueOf(25000)
        );
        when(reportService.getCategoryDetails(1L)).thenReturn(details);

        mockMvc.perform(get("/api/v1/finance/asset-cost-report/categories/1")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.categoryId").value(1))
                .andExpect(jsonPath("$.data.averageAssetValue").value(9474))
                .andExpect(jsonPath("$.data.maintenanceCost").value(25000));
    }

    @Test
    public void testGetCategoryAssetsSuccess() throws Exception {
        mockAuthSuccess();
        CategoryAssetItem assetItem = new CategoryAssetItem(
                101L,
                "AST-1001",
                "Logitech MX Master 3S",
                BigDecimal.valueOf(15000),
                BigDecimal.valueOf(12000),
                LocalDate.of(2024, 1, 10),
                "ASSIGNED"
        );
        CategoryAssetsResponse responseObj = new CategoryAssetsResponse(1L, "Accessories", List.of(assetItem));
        when(reportService.getCategoryAssets(1L)).thenReturn(responseObj);

        mockMvc.perform(get("/api/v1/finance/asset-cost-report/categories/1/assets")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.categoryId").value(1))
                .andExpect(jsonPath("$.data.assets[0].assetTag").value("AST-1001"))
                .andExpect(jsonPath("$.data.assets[0].purchaseValue").value(15000));
    }

    @Test
    public void testGetAssetFinancialDetailsSuccess() throws Exception {
        mockAuthSuccess();
        AssetFinancialDetailsResponse details = new AssetFinancialDetailsResponse(
                101L,
                "AST-1001",
                "Dell XPS 15",
                "Laptop",
                "Dell",
                LocalDate.of(2021, 1, 10),
                BigDecimal.valueOf(120000),
                BigDecimal.valueOf(98000),
                BigDecimal.valueOf(98000),
                BigDecimal.valueOf(18000),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(5000),
                LocalDate.of(2024, 1, 10),
                false,
                "ACTIVE"
        );
        when(reportService.getAssetFinancialDetails(101L)).thenReturn(details);

        mockMvc.perform(get("/api/v1/finance/asset-cost-report/assets/101")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assetId").value(101))
                .andExpect(jsonPath("$.data.depreciationRate").value(15))
                .andExpect(jsonPath("$.data.replacementDue").value(false));
    }

    @Test
    public void testGetDepreciationReportSuccess() throws Exception {
        mockAuthSuccess();
        CategoryDepreciationItem catDep = new CategoryDepreciationItem("Laptops", 85, BigDecimal.valueOf(1800000));
        DepreciationReportResponse report = new DepreciationReportResponse("FY2025-26", BigDecimal.valueOf(4200000), List.of(catDep));
        when(reportService.getDepreciationReport()).thenReturn(report);

        mockMvc.perform(get("/api/v1/finance/asset-cost-report/depreciation")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.financialYear").value("FY2025-26"))
                .andExpect(jsonPath("$.data.totalDepreciation").value(4200000));
    }

    @Test
    public void testGetMaintenanceCostReportSuccess() throws Exception {
        mockAuthSuccess();
        CategoryMaintenanceItem catMaint = new CategoryMaintenanceItem("Laptops", BigDecimal.valueOf(250000));
        MaintenanceCostReportResponse report = new MaintenanceCostReportResponse(BigDecimal.valueOf(800000), 35, List.of(catMaint));
        when(reportService.getMaintenanceCostReport()).thenReturn(report);

        mockMvc.perform(get("/api/v1/finance/asset-cost-report/maintenance-cost")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalMaintenanceCost").value(800000))
                .andExpect(jsonPath("$.data.assetsUnderMaintenance").value(35));
    }

    @Test
    public void testGetReplacementDueAssetsSuccess() throws Exception {
        mockAuthSuccess();
        ReplacementDueAssetItem item = new ReplacementDueAssetItem(
                101L,
                "AST-1001",
                "Dell Latitude 5400",
                "Laptop",
                LocalDate.of(2020, 1, 15),
                BigDecimal.valueOf(10000),
                6,
                "HIGH"
        );
        ReplacementDueAssetsResponse responseObj = new ReplacementDueAssetsResponse(1, List.of(item));
        when(reportService.getReplacementDueAssets()).thenReturn(responseObj);

        mockMvc.perform(get("/api/v1/finance/asset-cost-report/replacement-due")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.count").value(1))
                .andExpect(jsonPath("$.data.assets[0].yearsInUse").value(6))
                .andExpect(jsonPath("$.data.assets[0].replacementPriority").value("HIGH"));
    }

    @Test
    public void testExportPdfSuccess() throws Exception {
        mockAuthSuccess();
        ExportReportResponse responseObj = new ExportReportResponse(
                "asset-cost-report-FY2025-26.pdf",
                "application/pdf",
                "/api/v1/files/asset-cost-report-FY2025-26.pdf",
                LocalDateTime.of(2026, 6, 2, 10, 30, 0)
        );
        when(reportService.exportPdf()).thenReturn(responseObj);

        mockMvc.perform(get("/api/v1/finance/asset-cost-report/export/pdf")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("asset-cost-report-FY2025-26.pdf"))
                .andExpect(jsonPath("$.data.downloadUrl").value("/api/v1/files/asset-cost-report-FY2025-26.pdf"));
    }

    @Test
    public void testExportCsvSuccess() throws Exception {
        mockAuthSuccess();
        ExportReportResponse responseObj = new ExportReportResponse(
                "asset-cost-report-FY2025-26.csv",
                "text/csv",
                "/api/v1/files/asset-cost-report-FY2025-26.csv",
                LocalDateTime.of(2026, 6, 2, 10, 30, 0)
        );
        when(reportService.exportCsv()).thenReturn(responseObj);

        mockMvc.perform(get("/api/v1/finance/asset-cost-report/export/csv")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("asset-cost-report-FY2025-26.csv"))
                .andExpect(jsonPath("$.data.downloadUrl").value("/api/v1/files/asset-cost-report-FY2025-26.csv"));
    }

    @Test
    public void testDownloadCsvFileSuccess() throws Exception {
        byte[] csvBytes = "Asset ID,Asset Tag\n101,AST-1001".getBytes();
        when(reportService.generateCsvExportBytes()).thenReturn(csvBytes);

        mockMvc.perform(get("/api/v1/files/asset-cost-report-FY2025-26.csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"asset-cost-report-FY2025-26.csv\""))
                .andExpect(content().bytes(csvBytes));
    }

    @Test
    public void testDownloadPdfFileSuccess() throws Exception {
        byte[] pdfBytes = "%PDF-1.4 mock pdf catalog content".getBytes();
        when(reportService.generatePdfExportBytes()).thenReturn(pdfBytes);

        mockMvc.perform(get("/api/v1/files/asset-cost-report-FY2025-26.pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"asset-cost-report-FY2025-26.pdf\""))
                .andExpect(content().bytes(pdfBytes));
    }

    @Test
    public void testGetDashboardUnauthorized() throws Exception {
        when(jwtService.validateAccessToken("mock-token")).thenReturn(false);

        mockMvc.perform(get("/api/v1/finance/asset-cost-report/dashboard")
                .header("Authorization", token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetDashboardForbidden() throws Exception {
        User regularEmployee = new User();
        regularEmployee.setWorkEmail("emp@example.com");
        Role role = new Role();
        role.setName("EMPLOYEE");
        regularEmployee.setRole(role);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn("emp@example.com");
        when(userRepository.findByWorkEmail("emp@example.com")).thenReturn(Optional.of(regularEmployee));
        when(roleService.hasRoleOrGreater(any(User.class), any(String.class))).thenReturn(false);
        when(roleService.hasPermission("emp@example.com", "reports.finance")).thenReturn(false);
        when(roleService.hasPermission("emp@example.com", "expense.manage")).thenReturn(false);

        mockMvc.perform(get("/api/v1/finance/asset-cost-report/dashboard")
                .header("Authorization", token))
                .andExpect(status().isForbidden());
    }
}
