package com.example.ems.support.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.support.dto.*;
import com.example.ems.support.entity.*;
import com.example.ems.support.service.PlatformSupportCategoryService;
import com.example.ems.security.service.JwtService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class CategoryWorkflowSimulationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock private PlatformSupportCategoryService categoryService;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private RoleService roleService;

    @InjectMocks
    private PlatformSupportCategoryController controller;

    private final String adminEmail = "admin@company.com";
    private final String token = "admin-mock-token";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setWorkEmail(adminEmail);
        adminUser.setFullName("Platform Admin");

        when(jwtService.validateAccessToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(adminEmail);
        when(userRepository.findByWorkEmail(adminEmail)).thenReturn(Optional.of(adminUser));
        when(roleService.isSuperAdmin(adminEmail)).thenReturn(true);
    }

    @Test
    public void runCategoriesWorkflowSimulation() throws Exception {
        System.out.println("================================================================================");
        System.out.println("                 SUPPORT CATEGORIES MODULE WORKFLOW SIMULATION                 ");
        System.out.println("================================================================================");

        // STEP 1: CREATE NEW CATEGORY
        PlatformCategoryRequest createReq = new PlatformCategoryRequest();
        createReq.setName("Inventory");
        createReq.setDescription("Inventory and stock master issues.");
        createReq.setIcon("inventory");
        createReq.setColor("#10B981");
        createReq.setStatus("ACTIVE");

        MySupportCategory mockCreated = new MySupportCategory(5L, "Inventory", "inventory");
        mockCreated.setDescription(createReq.getDescription());
        mockCreated.setColor(createReq.getColor());
        mockCreated.setStatus(CategoryStatus.ACTIVE);
        mockCreated.setDisplayOrder(5);

        when(categoryService.createCategory(any(PlatformCategoryRequest.class), eq(adminEmail))).thenReturn(mockCreated);

        String createJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(createReq);
        System.out.println("\n[HTTP REQUEST] POST /api/platform/support/categories");
        System.out.println("Headers: Authorization = Bearer admin-mock-token");
        System.out.println("Body:\n" + createJson);

        MvcResult createResult = mockMvc.perform(post("/api/platform/support/categories")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createJson))
                .andReturn();

        String createResponseJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(createResult.getResponse().getContentAsString())
        );
        System.out.println("\n[HTTP RESPONSE] Status: 201 Created");
        System.out.println("Body:\n" + createResponseJson);
        System.out.println("--------------------------------------------------------------------------------");

        // STEP 2: GET DASHBOARD STATISTICS
        PlatformCategoryStatsResponse mockStats = new PlatformCategoryStatsResponse(
                5L, 4L, 1L, new PlatformCategoryStatsResponse.MostUsedCategory(1L, "Technical", 512L, 42.0)
        );

        when(categoryService.getDashboardStats()).thenReturn(mockStats);

        System.out.println("\n[HTTP REQUEST] GET /api/platform/support/categories/stats");
        System.out.println("Headers: Authorization = Bearer admin-mock-token");

        MvcResult statsResult = mockMvc.perform(get("/api/platform/support/categories/stats")
                .header("Authorization", "Bearer " + token))
                .andReturn();

        String statsResponseJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(statsResult.getResponse().getContentAsString())
        );
        System.out.println("\n[HTTP RESPONSE] Status: 200 OK");
        System.out.println("Body:\n" + statsResponseJson);
        System.out.println("--------------------------------------------------------------------------------");

        // STEP 3: GET OPTIONS DROPDOWN
        List<PlatformCategoryOption> mockOptions = List.of(
                new PlatformCategoryOption(1L, "Technical", "#2563EB", "tool"),
                new PlatformCategoryOption(5L, "Inventory", "#10B981", "inventory")
        );

        when(categoryService.getOptions()).thenReturn(mockOptions);

        System.out.println("\n[HTTP REQUEST] GET /api/platform/support/categories/options");
        System.out.println("Headers: Authorization = Bearer admin-mock-token");

        MvcResult optionsResult = mockMvc.perform(get("/api/platform/support/categories/options")
                .header("Authorization", "Bearer " + token))
                .andReturn();

        String optionsResponseJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(optionsResult.getResponse().getContentAsString())
        );
        System.out.println("\n[HTTP RESPONSE] Status: 200 OK");
        System.out.println("Body:\n" + optionsResponseJson);
        System.out.println("--------------------------------------------------------------------------------");

        // STEP 4: GET CATEGORIES TABLE
        when(categoryService.getCategories(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(mockCreated), PageRequest.of(0, 10), 1L));

        System.out.println("\n[HTTP REQUEST] GET /api/platform/support/categories?page=0&limit=10");
        System.out.println("Headers: Authorization = Bearer admin-mock-token");

        MvcResult tableResult = mockMvc.perform(get("/api/platform/support/categories")
                .header("Authorization", "Bearer " + token)
                .param("page", "0")
                .param("limit", "10"))
                .andReturn();

        String tableResponseJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(tableResult.getResponse().getContentAsString())
        );
        System.out.println("\n[HTTP RESPONSE] Status: 200 OK");
        System.out.println("Body:\n" + tableResponseJson);
        System.out.println("--------------------------------------------------------------------------------");

        // STEP 5: REORDER CATEGORIES
        PlatformCategoryReorderRequest reorderReq = new PlatformCategoryReorderRequest();
        reorderReq.setCategories(List.of(
                new PlatformCategoryReorderRequest.CategoryOrderDto(1L, 1),
                new PlatformCategoryReorderRequest.CategoryOrderDto(5L, 2)
        ));

        when(categoryService.reorderCategories(any())).thenReturn(List.of(
                new MySupportCategory(1L, "Technical", "tool"),
                mockCreated
        ));

        String reorderJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(reorderReq);
        System.out.println("\n[HTTP REQUEST] PATCH /api/platform/support/categories/reorder");
        System.out.println("Headers: Authorization = Bearer admin-mock-token");
        System.out.println("Body:\n" + reorderJson);

        MvcResult reorderResult = mockMvc.perform(patch("/api/platform/support/categories/reorder")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reorderJson))
                .andReturn();

        String reorderResponseJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(reorderResult.getResponse().getContentAsString())
        );
        System.out.println("\n[HTTP RESPONSE] Status: 200 OK");
        System.out.println("Body:\n" + reorderResponseJson);
        System.out.println("--------------------------------------------------------------------------------");

        // STEP 6: CHANGE STATUS TO INACTIVE
        when(categoryService.changeStatus(eq(5L), eq("INACTIVE"), eq(adminEmail))).thenReturn(mockCreated);

        System.out.println("\n[HTTP REQUEST] PATCH /api/platform/support/categories/5/status?status=INACTIVE");
        System.out.println("Headers: Authorization = Bearer admin-mock-token");

        MvcResult statusResult = mockMvc.perform(patch("/api/platform/support/categories/5/status")
                .header("Authorization", "Bearer " + token)
                .param("status", "INACTIVE"))
                .andReturn();

        String statusResponseJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(statusResult.getResponse().getContentAsString())
        );
        System.out.println("\n[HTTP RESPONSE] Status: 200 OK");
        System.out.println("Body:\n" + statusResponseJson);
        System.out.println("--------------------------------------------------------------------------------");

        // STEP 7: GET CATEGORY ANALYTICS
        List<PlatformCategoryAnalyticsResponse> mockAnalytics = List.of(
                new PlatformCategoryAnalyticsResponse("Technical", 512L, 42.0, "2.4 hrs", 112L, 400L),
                new PlatformCategoryAnalyticsResponse("Inventory", 246L, 20.0, "1.8 hrs", 46L, 200L)
        );

        when(categoryService.getAnalytics()).thenReturn(mockAnalytics);

        System.out.println("\n[HTTP REQUEST] GET /api/platform/support/categories/analytics");
        System.out.println("Headers: Authorization = Bearer admin-mock-token");

        MvcResult analyticsResult = mockMvc.perform(get("/api/platform/support/categories/analytics")
                .header("Authorization", "Bearer " + token))
                .andReturn();

        String analyticsResponseJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(analyticsResult.getResponse().getContentAsString())
        );
        System.out.println("\n[HTTP RESPONSE] Status: 200 OK");
        System.out.println("Body:\n" + analyticsResponseJson);
        System.out.println("================================================================================");
    }
}
