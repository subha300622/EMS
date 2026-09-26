package com.example.ems.reports.controller;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.reports.common.ExportFormat;
import com.example.ems.reports.common.ReportExportStatus;
import com.example.ems.reports.export.ReportExportHistory;
import com.example.ems.reports.organization.ReportFacade;
import com.example.ems.reports.organization.controller.PlatformOrganizationReportController;
import com.example.ems.reports.organization.dto.ExportHistoryResponse;
import com.example.ems.reports.organization.dto.OrganizationReportDetail;
import com.example.ems.reports.organization.dto.OrganizationReportListItem;
import com.example.ems.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PlatformOrganizationReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReportFacade reportFacade;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private PlatformOrganizationReportController controller;

    private static final String TOKEN = "admin-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "platform.admin@example.com";

    private User adminUser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        adminUser = new User();
        adminUser.setWorkEmail(EMAIL);
        Role role = new Role();
        role.setName("SUPER_ADMIN");
        adminUser.setRole(role);

        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);
        when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(adminUser));
        when(roleService.hasPermission(eq(EMAIL), any())).thenReturn(true);
    }

    @Test
    public void testGetOrganizationList() throws Exception {
        OrganizationReportListItem item = new OrganizationReportListItem(1L, "ORG-1001", "Acme", "acme@example.com", "ACTIVE", "PREMIUM", 10, 8, "2026-01-01");
        Page<OrganizationReportListItem> page = new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);
        when(reportFacade.getOrganizationList(any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/platform/reports/organizations/list")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].organizationName").value("Acme"));
    }

    @Test
    public void testGetTop() throws Exception {
        OrganizationReportListItem item = new OrganizationReportListItem(1L, "ORG-1001", "Acme", "acme@example.com", "ACTIVE", "PREMIUM", 10, 8, "2026-01-01");
        when(reportFacade.getTopOrganizations("employees", 10)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/platform/reports/organizations/top")
                        .param("sortBy", "employees")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].organizationName").value("Acme"));
    }

    @Test
    public void testGetDetails() throws Exception {
        OrganizationReportDetail detail = new OrganizationReportDetail();
        detail.setOrganizationId(1L);
        detail.setOrganizationName("Acme");
        detail.setDepartmentCount(5);
        detail.setRevenue(350.0);
        detail.setAuditSummary(new HashMap<>());

        when(reportFacade.getOrganizationDetails(1L)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/platform/reports/organizations/1")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.organizationName").value("Acme"))
                .andExpect(jsonPath("$.data.departmentCount").value(5));
    }

    @Test
    public void testExport() throws Exception {
        ReportExportHistory history = new ReportExportHistory();
        history.setId(1L);
        history.setStatus(ReportExportStatus.PENDING);

        when(reportFacade.exportReport(eq(ExportFormat.CSV), any(), any(), any(), eq(EMAIL))).thenReturn(history);

        mockMvc.perform(post("/api/v1/platform/reports/organizations/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"format\":\"CSV\"}")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.exportId").value(1));
    }

    @Test
    public void testGetExports() throws Exception {
        ExportHistoryResponse response = new ExportHistoryResponse(1L, "Report", EMAIL, "ORGANIZATION", "CSV", "COMPLETED", "url", "2026-07-03");
        Page<ExportHistoryResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(reportFacade.getExportHistory(eq(EMAIL), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/platform/reports/organizations/exports")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].reportName").value("Report"));
    }

    @Test
    public void testDownload() throws Exception {
        ReportExportHistory record = new ReportExportHistory();
        record.setId(1L);
        record.setStatus(ReportExportStatus.COMPLETED);
        record.setReportName("Report");
        record.setExportFormat(ExportFormat.CSV);

        when(reportFacade.getExportById(1L)).thenReturn(record);
        when(reportFacade.getExportFile(1L)).thenReturn(new ByteArrayInputStream("ID,Name\n1,Acme".getBytes()));

        mockMvc.perform(get("/api/v1/platform/reports/organizations/export/download/1")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String content = result.getResponse().getContentAsString();
                    assert content.contains("ID,Name");
                });
    }
}
