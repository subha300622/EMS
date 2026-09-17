package com.example.ems.offboarding.controller;

import com.example.ems.offboarding.dto.InitiateExitRequest;
import com.example.ems.offboarding.dto.InitiateExitResponse;
import com.example.ems.offboarding.dto.OffboardingAnalyticsResponse;
import com.example.ems.offboarding.dto.OffboardingRequestSummaryDto;
import com.example.ems.offboarding.service.OffboardingDashboardService;
import com.example.ems.offboarding.service.OffboardingService;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OffboardingDashboardControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OffboardingDashboardService dashboardService;

    @Mock
    private OffboardingService offboardingService;

    @InjectMocks
    private OffboardingRequestController controller;

    private static final Long ORG_ID = 100L;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        TenantContext.setCurrentTenant(ORG_ID);
    }

    @AfterEach
    public void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("GET /api/v1/offboarding/requests - Success with valid parameters")
    void testGetRequests_Success() throws Exception {
        OffboardingRequestSummaryDto dto = new OffboardingRequestSummaryDto(
                1L, 25L, "John Doe", "EMP025", "Senior Engineer", "Engineering",
                "RESIGNATION", "ACTIVE", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), "FINANCE_CLEARANCE"
        );
        Page<OffboardingRequestSummaryDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);

        when(dashboardService.getRequests(eq("ACTIVE"), eq("John"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/offboarding/requests")
                        .param("status", "ACTIVE")
                        .param("search", "John")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].employeeName").value("John Doe"))
                .andExpect(jsonPath("$.data.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/offboarding/requests - 400 for negative page")
    void testGetRequests_InvalidPage() throws Exception {
        mockMvc.perform(get("/api/v1/offboarding/requests")
                        .param("page", "-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VAL_001"));
    }

    @Test
    @DisplayName("GET /api/v1/offboarding/requests - 400 for invalid size")
    void testGetRequests_InvalidSize() throws Exception {
        mockMvc.perform(get("/api/v1/offboarding/requests")
                        .param("size", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VAL_001"));
    }

    @Test
    @DisplayName("GET /api/v1/offboarding/analytics - Success")
    void testGetAnalytics_Success() throws Exception {
        OffboardingAnalyticsResponse analytics = new OffboardingAnalyticsResponse(
                5, 12, 3, 20, 16, 4, 30.0, 28.0
        );

        when(dashboardService.getAnalytics(eq(LocalDate.of(2026, 1, 1)), eq(LocalDate.of(2026, 9, 15))))
                .thenReturn(analytics);

        mockMvc.perform(get("/api/v1/offboarding/analytics")
                        .param("from", "2026-01-01")
                        .param("to", "2026-09-15")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.active").value(5))
                .andExpect(jsonPath("$.data.completed").value(12))
                .andExpect(jsonPath("$.data.scheduled").value(3))
                .andExpect(jsonPath("$.data.totalRequests").value(20))
                .andExpect(jsonPath("$.data.averageNoticePeriodDays").value(30.0));
    }

    @Test
    @DisplayName("GET /api/v1/offboarding/analytics - 400 when from > to")
    void testGetAnalytics_InvalidDateRange() throws Exception {
        mockMvc.perform(get("/api/v1/offboarding/analytics")
                        .param("from", "2026-09-15")
                        .param("to", "2026-01-01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VAL_001"));
    }

    @Test
    @DisplayName("POST /api/v1/offboarding/requests - Success 201 created")
    void testInitiateExit_Success() throws Exception {
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        InitiateExitRequest req = new InitiateExitRequest(
                25L, "RESIGNATION", LocalDate.of(2026, 10, 10), LocalDate.of(2026, 9, 15),
                30, "CAREER_GROWTH", "Moving to new job", 12L, List.of("EMPLOYEE", "HR")
        );

        InitiateExitResponse resp = new InitiateExitResponse(
                101L, 25L, "John Doe", "EMP025", "RESIGNATION", "PENDING", "MANAGER_APPROVAL",
                4L, "Engineering Standard Offboarding", LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 9, 15), "CAREER_GROWTH", 12L, LocalDateTime.now()
        );

        when(offboardingService.initiateExit(any(InitiateExitRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/offboarding/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.offboardingId").value(101L))
                .andExpect(jsonPath("$.data.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.data.templateId").value(4L))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/v1/offboarding/requests - 400 for null employeeId")
    void testInitiateExit_NullEmployeeId() throws Exception {
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        InitiateExitRequest req = new InitiateExitRequest();
        req.setLastWorkingDate(LocalDate.of(2026, 10, 10));

        mockMvc.perform(post("/api/v1/offboarding/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/offboarding/requests/101/template - Success")
    void testAssignTemplateToRequest_Success() throws Exception {
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        com.example.ems.offboarding.dto.AssignTemplateToRequestDto req =
                new com.example.ems.offboarding.dto.AssignTemplateToRequestDto(4L);

        InitiateExitResponse resp = new InitiateExitResponse(
                101L, 25L, "John Doe", "EMP025", "RESIGNATION", "PENDING", "MANAGER_APPROVAL",
                4L, "Engineering Standard Offboarding", LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 9, 15), "CAREER_GROWTH", null, LocalDateTime.now()
        );

        when(offboardingService.assignTemplateToRequest(eq(101L), any(com.example.ems.offboarding.dto.AssignTemplateToRequestDto.class)))
                .thenReturn(resp);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/offboarding/requests/101/template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.offboardingId").value(101L))
                .andExpect(jsonPath("$.data.templateId").value(4L))
                .andExpect(jsonPath("$.data.templateName").value("Engineering Standard Offboarding"));
    }
}
