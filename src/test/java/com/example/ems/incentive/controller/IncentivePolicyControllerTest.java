package com.example.ems.incentive.controller;

import com.example.ems.incentive.dto.IncentivePolicyRequest;
import com.example.ems.incentive.dto.IncentivePolicyResponse;
import com.example.ems.incentive.dto.IncentiveSlabTierDto;
import com.example.ems.incentive.entity.IncentiveCalculationMethod;
import com.example.ems.incentive.entity.IncentivePaymentFrequency;
import com.example.ems.incentive.entity.IncentivePolicyStatus;
import com.example.ems.incentive.entity.IncentiveType;
import com.example.ems.incentive.service.IncentivePolicyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
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

@ExtendWith(MockitoExtension.class)
public class IncentivePolicyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IncentivePolicyService policyService;

    @InjectMocks
    private IncentivePolicyController policyController;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(policyController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void testCreatePolicy() throws Exception {
        IncentivePolicyRequest req = new IncentivePolicyRequest();
        req.setPolicyCode("INC-TEST-01");
        req.setPolicyName("Q1 Sales Performance");
        req.setIncentiveType(IncentiveType.SALES);
        req.setCalculationMethod(IncentiveCalculationMethod.TARGET_SLAB);
        req.setPaymentFrequency(IncentivePaymentFrequency.MONTHLY);
        req.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        req.setTargetSlabs(List.of(
                new IncentiveSlabTierDto(BigDecimal.valueOf(80), BigDecimal.valueOf(100), BigDecimal.valueOf(5000), null),
                new IncentiveSlabTierDto(BigDecimal.valueOf(100), BigDecimal.valueOf(120), BigDecimal.valueOf(10000), null)
        ));

        IncentivePolicyResponse res = IncentivePolicyResponse.builder()
                .id(10L)
                .policyCode("INC-TEST-01")
                .policyName("Q1 Sales Performance")
                .incentiveType(IncentiveType.SALES)
                .calculationMethod(IncentiveCalculationMethod.TARGET_SLAB)
                .paymentFrequency(IncentivePaymentFrequency.MONTHLY)
                .status(IncentivePolicyStatus.DRAFT)
                .build();

        when(policyService.createPolicy(any(IncentivePolicyRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/incentive-policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.policyCode").value("INC-TEST-01"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    void testGetPolicyById() throws Exception {
        IncentivePolicyResponse res = IncentivePolicyResponse.builder()
                .id(10L)
                .policyCode("INC-TEST-01")
                .policyName("Q1 Sales Performance")
                .status(IncentivePolicyStatus.ACTIVE)
                .build();

        when(policyService.getPolicyById(10L)).thenReturn(res);

        mockMvc.perform(get("/api/v1/incentive-policies/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.policyCode").value("INC-TEST-01"));
    }

    @Test
    void testGetAllPolicies() throws Exception {
        IncentivePolicyResponse res = IncentivePolicyResponse.builder()
                .id(10L)
                .policyCode("INC-TEST-01")
                .policyName("Q1 Sales Performance")
                .build();

        when(policyService.getAllPolicies(any(), any()))
                .thenReturn(new PageImpl<>(List.of(res), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/incentive-policies?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(10));
    }

    @Test
    void testActivatePolicy() throws Exception {
        IncentivePolicyResponse res = IncentivePolicyResponse.builder()
                .id(10L)
                .status(IncentivePolicyStatus.ACTIVE)
                .build();

        when(policyService.activatePolicy(10L)).thenReturn(res);

        mockMvc.perform(post("/api/v1/incentive-policies/10/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void testDeactivatePolicy() throws Exception {
        IncentivePolicyResponse res = IncentivePolicyResponse.builder()
                .id(10L)
                .status(IncentivePolicyStatus.INACTIVE)
                .build();

        when(policyService.deactivatePolicy(10L)).thenReturn(res);

        mockMvc.perform(post("/api/v1/incentive-policies/10/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    void testArchivePolicy() throws Exception {
        IncentivePolicyResponse res = IncentivePolicyResponse.builder()
                .id(10L)
                .status(IncentivePolicyStatus.ARCHIVED)
                .build();

        when(policyService.archivePolicy(10L)).thenReturn(res);

        mockMvc.perform(post("/api/v1/incentive-policies/10/archive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ARCHIVED"));
    }
}
