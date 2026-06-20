package com.example.ems.asset.controller;

import com.example.ems.asset.dto.*;
import com.example.ems.asset.entity.MyAsset;
import com.example.ems.asset.repository.MyAssetRepository;
import com.example.ems.asset.service.MyAssetService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MyAssetControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MyAssetService assetService;

    @Mock
    private MyAssetRepository assetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private MyAssetController myAssetController;

    private static final String TOKEN = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "employee@example.com";
    private Employee mockEmployee;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(myAssetController).build();

        // Standard auth mock setup
        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);

        User user = new User();
        user.setWorkEmail(EMAIL);
        when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(user));

        mockEmployee = new Employee();
        mockEmployee.setId(1L);
        mockEmployee.setEmployeeId("EMP001");
        mockEmployee.setFullName("John Doe");
        mockEmployee.setEmail(EMAIL);
        mockEmployee.setDepartment("Engineering");
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockEmployee));
    }

    private void mockPermission(String permission, boolean allowed) {
        when(roleService.hasPermission(EMAIL, permission)).thenReturn(allowed);
    }



    @Test
    public void testGetAssignedAssetsSuccess() throws Exception {
        mockPermission("asset.self.read", true);

        MyAssignedAssetItem item = new MyAssignedAssetItem(
                101L, "SN-DL2024-421", "Dell XPS 15", "LAPTOP", "Dell", "XPS 15 9530", "CN-0XX123-DELL1",
                LocalDate.now().minusMonths(6), BigDecimal.valueOf(120000.00), BigDecimal.valueOf(115000.00),
                LocalDate.now().minusMonths(6), "EXCELLENT", "ASSIGNED"
        );
        Page<MyAssignedAssetItem> page = new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);

        when(assetService.getAssignedAssets(any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/my-assets")
                .header("Authorization", AUTH_HEADER)
                .param("status", "ASSIGNED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].assetName").value("Dell XPS 15"));
    }

    @Test
    public void testGetAssetDetailsSuccess() throws Exception {
        mockPermission("asset.self.read", true);

        MyAsset asset = new MyAsset();
        asset.setId(101L);
        asset.setAssignedTo(mockEmployee);
        when(assetRepository.findById(101L)).thenReturn(Optional.of(asset));

        MyAssetDetailsResponse details = new MyAssetDetailsResponse(
                101L, "SN-DL2024-421", "Dell XPS 15", "LAPTOP", "Dell", "XPS 15 9530", "CN-0XX123-DELL1",
                LocalDate.now().minusMonths(6), BigDecimal.valueOf(120000.00), BigDecimal.valueOf(115000.00),
                LocalDate.now().minusMonths(6), "IT Admin", "Headquarters", "EXCELLENT", "ACTIVE",
                LocalDate.now().plusYears(2), "ASSIGNED", LocalDateTime.now()
        );
        when(assetService.getAssetDetails(eq(101L), any())).thenReturn(details);

        mockMvc.perform(get("/api/v1/my-assets/101")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assetName").value("Dell XPS 15"));
    }

    @Test
    public void testSubmitAssetRequestSuccess() throws Exception {
        mockPermission("asset.self.request", true);

        CreateAssetRequest request = new CreateAssetRequest("LAPTOP", "Dell XPS 15", "Business work", "HIGH", LocalDate.now().plusDays(5));
        AssetRequestResponse response = new AssetRequestResponse(
                1L, "REQ-ASSET-XYZ123", "LAPTOP", "Dell XPS 15", "Business work", "HIGH",
                LocalDate.now().plusDays(5), null, "PENDING_MANAGER_APPROVAL", LocalDateTime.now(), LocalDate.now().plusDays(7), "Line Manager"
        );

        when(assetService.requestAsset(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/my-assets/requests")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.requestNumber").value("REQ-ASSET-XYZ123"));
    }

    @Test
    public void testGetAssetRequestsSuccess() throws Exception {
        mockPermission("asset.self.read", true);

        AssetRequestResponse req = new AssetRequestResponse(
                1L, "REQ-ASSET-XYZ123", "LAPTOP", "Dell XPS 15", "Business work", "HIGH",
                LocalDate.now().plusDays(5), null, "PENDING_MANAGER_APPROVAL", LocalDateTime.now(), LocalDate.now().plusDays(7), "Line Manager"
        );
        AssetRequestsListResponse response = new AssetRequestsListResponse(List.of(req));

        when(assetService.getAssetRequests(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/my-assets/requests")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.requests[0].requestNumber").value("REQ-ASSET-XYZ123"));
    }

    @Test
    public void testReportIssueSuccess() throws Exception {
        mockPermission("asset.self.issue.create", true);

        MyAsset asset = new MyAsset();
        asset.setId(101L);
        asset.setAssignedTo(mockEmployee);
        when(assetRepository.findById(101L)).thenReturn(Optional.of(asset));

        ReportIssueRequest req = new ReportIssueRequest("HARDWARE", "HIGH", "Screen Flicker", "Monitor flickering since yesterday");
        ReportIssueResponse response = new ReportIssueResponse(
                1L, "TKT-XYZ123", 101L, "Dell XPS 15", "HARDWARE", "HIGH", "Screen Flicker",
                "Monitor flickering since yesterday", "OPEN", LocalDateTime.now(), "IT Support Team", LocalDate.now().plusDays(3)
        );

        when(assetService.reportIssue(eq(101L), any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/my-assets/101/issues")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ticketId").value("TKT-XYZ123"));
    }

    @Test
    public void testGetAssetIssuesSuccess() throws Exception {
        mockPermission("asset.self.read", true);

        ReportIssueResponse issue = new ReportIssueResponse(
                1L, "TKT-XYZ123", 101L, "Dell XPS 15", "HARDWARE", "HIGH", "Screen Flicker",
                "Monitor flickering since yesterday", "OPEN", LocalDateTime.now(), "IT Support Team", LocalDate.now().plusDays(3)
        );
        AssetIssuesListResponse response = new AssetIssuesListResponse(List.of(issue));

        when(assetService.getAssetIssues(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/my-assets/issues")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.issues[0].ticketId").value("TKT-XYZ123"));
    }

    @Test
    public void testSubmitReturnRequestSuccess() throws Exception {
        mockPermission("asset.self.return.request", true);

        MyAsset asset = new MyAsset();
        asset.setId(101L);
        asset.setAssignedTo(mockEmployee);
        when(assetRepository.findById(101L)).thenReturn(Optional.of(asset));

        AssetReturnFormRequest request = new AssetReturnFormRequest("No longer required", "EXCELLENT", List.of("Charger", "Cable"), "None");
        AssetReturnResponse response = new AssetReturnResponse(
                1L, "RET-XYZ123", 101L, "Dell XPS 15", "No longer required", "EXCELLENT",
                List.of("Charger", "Cable"), "None", "PENDING_IT_VERIFICATION", LocalDateTime.now()
        );

        when(assetService.submitReturnRequest(eq(101L), any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/my-assets/101/return-request")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.returnReference").value("RET-XYZ123"));
    }

    @Test
    public void testGetAssetTimelineSuccess() throws Exception {
        mockPermission("asset.self.timeline.read", true);

        MyAsset asset = new MyAsset();
        asset.setId(101L);
        asset.setAssignedTo(mockEmployee);
        when(assetRepository.findById(101L)).thenReturn(Optional.of(asset));

        AssetTimelineResponse.TimelineEventItem event = new AssetTimelineResponse.TimelineEventItem("ASSIGNED", "IT Admin", LocalDateTime.now(), "Initial assignment");
        AssetTimelineResponse response = new AssetTimelineResponse(List.of(event));

        when(assetService.getAssetTimeline(eq(101L), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/my-assets/101/timeline")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.events[0].event").value("ASSIGNED"));
    }

    @Test
    public void testGetCategoriesSuccess() throws Exception {
        mockPermission("asset.self.read", true);

        AssetCategoriesResponse.CategoryItem item = new AssetCategoriesResponse.CategoryItem(1L, "LAPTOP", "Laptop", 1, true);
        AssetCategoriesResponse response = new AssetCategoriesResponse(List.of(item));

        when(assetService.getCategories()).thenReturn(response);

        mockMvc.perform(get("/api/v1/my-assets/categories")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.categories[0].code").value("LAPTOP"));
    }

    @Test
    public void testGetPoliciesSuccess() throws Exception {
        mockPermission("asset.self.read", true);

        AssetPoliciesResponse.PolicyItem item = new AssetPoliciesResponse.PolicyItem(1L, "Usage Policy", "Rules description", false);
        AssetPoliciesResponse response = new AssetPoliciesResponse(List.of(item));

        when(assetService.getPolicies()).thenReturn(response);

        mockMvc.perform(get("/api/v1/my-assets/policies")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.policies[0].title").value("Usage Policy"));
    }
}
