package com.example.ems.overtime.controller;

import com.example.ems.overtime.dto.OvertimePolicyRequest;
import com.example.ems.overtime.dto.OvertimePolicyResponse;
import com.example.ems.overtime.entity.OvertimeAmountBasis;
import com.example.ems.overtime.entity.OvertimePolicyStatus;
import com.example.ems.overtime.service.OvertimePolicyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

@ExtendWith(MockitoExtension.class)
public class OvertimePolicyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OvertimePolicyService policyService;

    @InjectMocks
    private OvertimePolicyController policyController;

    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(policyController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void testCreatePolicy() throws Exception {
        OvertimePolicyRequest req = new OvertimePolicyRequest();
        req.setName("Tech OT Policy");
        req.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        req.setNormalWorkingHours(8);
        req.setMinimumOtMinutes(30);
        req.setMaximumOtMinutes(240);
        req.setAmountBasis(OvertimeAmountBasis.BASIC_SALARY);
        req.setNormalDayMultiplier(BigDecimal.valueOf(1.50));
        req.setWeekendMultiplier(BigDecimal.valueOf(2.00));
        req.setHolidayMultiplier(BigDecimal.valueOf(2.00));
        req.setApprovalRequired(true);

        OvertimePolicyResponse res = new OvertimePolicyResponse();
        res.setId(10L);
        res.setName("Tech OT Policy");
        res.setStatus(OvertimePolicyStatus.DRAFT);

        when(policyService.createPolicy(any(OvertimePolicyRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/overtime/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.name").value("Tech OT Policy"));
    }

    @Test
    void testGetPolicyById() throws Exception {
        OvertimePolicyResponse res = new OvertimePolicyResponse();
        res.setId(10L);
        res.setName("Tech OT Policy");

        when(policyService.getPolicyById(10L)).thenReturn(res);

        mockMvc.perform(get("/api/v1/overtime/policies/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10));
    }

    @Test
    void testGetAllPolicies() throws Exception {
        OvertimePolicyResponse res = new OvertimePolicyResponse();
        res.setId(10L);
        res.setName("Tech OT Policy");

        when(policyService.getAllPolicies(any(), any()))
                .thenReturn(new PageImpl<>(List.of(res), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/overtime/policies?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(10));
    }

    @Test
    void testActivatePolicy() throws Exception {
        OvertimePolicyResponse res = new OvertimePolicyResponse();
        res.setId(10L);
        res.setStatus(OvertimePolicyStatus.ACTIVE);

        when(policyService.activatePolicy(10L)).thenReturn(res);

        mockMvc.perform(post("/api/v1/overtime/policies/10/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }
}
