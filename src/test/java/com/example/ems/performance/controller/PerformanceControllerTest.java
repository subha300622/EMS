package com.example.ems.performance.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.performance.dto.PerformanceCycleRequest;
import com.example.ems.performance.dto.PerformanceCycleResponse;
import com.example.ems.performance.entity.PerformanceCycle;
import com.example.ems.performance.service.PerformanceService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PerformanceControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PerformanceService performanceService;
    @Mock
    private RoleService roleService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private PerformanceController performanceController;

    private User hrUser;
    private User empUser;
    private final String hrEmail = "hr@example.com";
    private final String empEmail = "emp@example.com";
    private final String token = "mock-token";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(performanceController).build();

        hrUser = new User();
        hrUser.setWorkEmail(hrEmail);

        empUser = new User();
        empUser.setWorkEmail(empEmail);
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

    // ── CYCLES ─────────────────────────────────────────────────────────────
    @Test
    public void testGetCyclesSuccess() throws Exception {
        setupEmployee();

        PerformanceCycle cycle = new PerformanceCycle();
        cycle.setId(1L);
        cycle.setName("Q1 2026");
        cycle.setStatus("ACTIVE");

        PerformanceCycleResponse resp = new PerformanceCycleResponse(cycle);
        resp.enrichGoalStats(0, 0, 0, 0);
        resp.enrichReviewStats(0, 0);

        when(performanceService.getCycles()).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/v1/performance/reviews/cycles")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Q1 2026"));
    }

    @Test
    public void testCreateCycleSuccess() throws Exception {
        setupManager();

        PerformanceCycleRequest req = new PerformanceCycleRequest();
        req.setName("Q2 2026");
        req.setStartDate(LocalDate.of(2026, 4, 1));
        req.setEndDate(LocalDate.of(2026, 6, 30));

        PerformanceCycle cycle = new PerformanceCycle();
        cycle.setId(2L);
        cycle.setName("Q2 2026");
        cycle.setStatus("ACTIVE");

        PerformanceCycleResponse resp = new PerformanceCycleResponse(cycle);
        resp.enrichGoalStats(0, 0, 0, 0);
        resp.enrichReviewStats(0, 0);

        when(performanceService.createCycle(any(PerformanceCycleRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/performance/reviews/cycles")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Q2 2026"));
    }

    @Test
    public void testUpdateCycleSuccess() throws Exception {
        setupManager();

        PerformanceCycleRequest req = new PerformanceCycleRequest();
        req.setName("Q2 2026 Updated");
        req.setStartDate(LocalDate.of(2026, 4, 1));
        req.setEndDate(LocalDate.of(2026, 6, 30));
        req.setStatus("CLOSED");

        PerformanceCycle cycle = new PerformanceCycle();
        cycle.setId(2L);
        cycle.setName("Q2 2026 Updated");
        cycle.setStatus("CLOSED");

        PerformanceCycleResponse resp = new PerformanceCycleResponse(cycle);
        resp.enrichGoalStats(0, 0, 0, 0);
        resp.enrichReviewStats(0, 0);

        when(performanceService.updateCycle(eq(2L), any(PerformanceCycleRequest.class))).thenReturn(Optional.of(resp));

        mockMvc.perform(put("/api/v1/performance/reviews/cycles/2")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CLOSED"));
    }
}
