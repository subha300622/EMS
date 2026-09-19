package com.example.ems.organization.controller;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.dto.*;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.organization.service.OrganizationCacheService;
import com.example.ems.organization.service.OrganizationExportService;
import com.example.ems.organization.service.OrganizationService;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PlatformAdminOrganizationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private OrganizationCacheService organizationCacheService;

    @Mock
    private OrganizationExportService exportService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private PlatformAdminOrganizationController controller;

    private static final String TOKEN = "admin-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "platform.admin@example.com";
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

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
    public void testGetAllOrganizations() throws Exception {
        OrganizationListItemResponse item = new OrganizationListItemResponse(1L, "ORG-1001", "Acme", "acme@example.com", "123", "PREMIUM", 10, "ACTIVE", "2026-01-01");
        Page<OrganizationListItemResponse> page = new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);

        when(organizationCacheService.searchOrganizations(any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/platform-admin/organizations")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Acme"));
    }

    @Test
    public void testGetOrganizationDetails() throws Exception {
        OrganizationDetailResponse detail = new OrganizationDetailResponse(1L, "ORG-1001", "Acme", "acme@example.com", "123", "acme.com",
                new OrganizationAddressDto("Street", "City", "State", "Country", "123"),
                new OrganizationSubscriptionDto("PREMIUM", "ACTIVE", "2026-01-01", "2027-01-01"),
                10, 2, "2026-01-01");

        when(organizationCacheService.getOrganizationDetails(1L)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/platform-admin/organizations/1")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.website").value("acme.com"));
    }

    @Test
    public void testCreateOrganization() throws Exception {
        CreateOrganizationRequest req = new CreateOrganizationRequest();
        req.setName("NewOrg");
        req.setEmail("new@example.com");
        req.setSubscriptionPlan("PREMIUM");
        req.setAddress(new OrganizationAddressDto("Street", "City", "State", "Country", "123"));

        OrganizationDetailResponse detail = new OrganizationDetailResponse(2L, "ORG-1002", "NewOrg", "new@example.com", "123", "neworg.com",
                new OrganizationAddressDto("Street", "City", "State", "Country", "123"),
                new OrganizationSubscriptionDto("PREMIUM", "ACTIVE", "2026-01-01", "2027-01-01"),
                0, 0, "2026-01-01");

        when(organizationService.createOrganization(any(), any())).thenReturn(detail);

        mockMvc.perform(post("/api/v1/platform-admin/organizations")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("NewOrg"));
    }

    @Test
    public void testUpdateOrganization() throws Exception {
        UpdateOrganizationRequest req = new UpdateOrganizationRequest();
        req.setName("UpdatedOrg");

        OrganizationDetailResponse detail = new OrganizationDetailResponse(1L, "ORG-1001", "UpdatedOrg", "acme@example.com", "123", "acme.com",
                new OrganizationAddressDto("Street", "City", "State", "Country", "123"),
                new OrganizationSubscriptionDto("PREMIUM", "ACTIVE", "2026-01-01", "2027-01-01"),
                10, 2, "2026-01-01");

        when(organizationService.updateOrganization(eq(1L), any(), any())).thenReturn(detail);

        mockMvc.perform(put("/api/v1/platform-admin/organizations/1")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("UpdatedOrg"));
    }

    @Test
    public void testSuspendOrganization() throws Exception {
        doNothing().when(organizationService).suspendOrganization(eq(1L), any(), any());

        mockMvc.perform(patch("/api/v1/platform-admin/organizations/1/status")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("status", "SUSPENDED", "reason", "Late payment"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testActivateOrganization() throws Exception {
        doNothing().when(organizationService).activateOrganization(eq(1L), any());

        mockMvc.perform(patch("/api/v1/platform-admin/organizations/1/status")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("status", "ACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testUpdateSubscription() throws Exception {
        UpdateSubscriptionRequest req = new UpdateSubscriptionRequest();
        req.setPlan("ENTERPRISE");
        req.setExpiryDate(LocalDate.now().plusYears(1));

        doNothing().when(organizationService).updateSubscription(eq(1L), any(), any());

        mockMvc.perform(patch("/api/v1/platform-admin/organizations/1/subscription")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testDeleteOrganization() throws Exception {
        doNothing().when(organizationService).deleteOrganization(eq(1L), any());

        mockMvc.perform(delete("/api/v1/platform-admin/organizations/1")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetStatistics() throws Exception {
        OrganizationStatisticsResponse stats = new OrganizationStatisticsResponse(10, 5, 2, 8, 350.0, 1.5, "2026-07-01");
        when(organizationCacheService.getStatistics(1L)).thenReturn(stats);

        mockMvc.perform(get("/api/v1/platform-admin/organizations/1/statistics")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employees").value(10))
                .andExpect(jsonPath("$.data.departments").value(5));
    }

    @Test
    public void testGetSummary() throws Exception {
        OrganizationSummaryResponse summary = new OrganizationSummaryResponse(10, 8, 2, 0, 5, 3);
        when(organizationCacheService.getSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/v1/platform-admin/organizations/summary")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalOrganizations").value(10));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGlobalSearch() throws Exception {
        when(organizationRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());
        when(employeeRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());
        when(departmentRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());
        when(userRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/platform-admin/search")
                        .param("q", "test")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.organizations").isArray())
                .andExpect(jsonPath("$.data.employees").isArray());
    }

    @Test
    public void testAccessDenied() throws Exception {
        when(roleService.hasPermission(EMAIL, "organization.read")).thenReturn(false);

        mockMvc.perform(get("/api/v1/platform-admin/organizations")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isForbidden());
    }
}
