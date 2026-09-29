package com.example.ems.bonus.controller;

import com.example.ems.bonus.dto.BonusPolicyRequest;
import com.example.ems.bonus.dto.BonusPolicyResponse;
import com.example.ems.bonus.entity.BonusCalculationMethod;
import com.example.ems.bonus.entity.BonusPolicyStatus;
import com.example.ems.bonus.entity.BonusType;
import com.example.ems.bonus.service.BonusPolicyService;
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
public class BonusPolicyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BonusPolicyService policyService;

    @InjectMocks
    private BonusPolicyController policyController;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(policyController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void testCreatePolicy() throws Exception {
        BonusPolicyRequest req = new BonusPolicyRequest();
        req.setName("Annual Performance Bonus 2026");
        req.setBonusType(BonusType.PERFORMANCE);
        req.setCalculationMethod(BonusCalculationMethod.FIXED_AMOUNT);
        req.setFixedAmount(BigDecimal.valueOf(15000.00));
        req.setEffectiveFrom(LocalDate.of(2026, 4, 1));
        req.setEffectiveTo(LocalDate.of(2027, 3, 31));

        BonusPolicyResponse res = new BonusPolicyResponse();
        res.setId(5L);
        res.setName("Annual Performance Bonus 2026");
        res.setStatus(BonusPolicyStatus.DRAFT);
        res.setFixedAmount(BigDecimal.valueOf(15000.00));

        when(policyService.createPolicy(any(BonusPolicyRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/bonuses/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.name").value("Annual Performance Bonus 2026"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    void testGetPolicyById() throws Exception {
        BonusPolicyResponse res = new BonusPolicyResponse();
        res.setId(5L);
        res.setName("Annual Performance Bonus 2026");
        res.setStatus(BonusPolicyStatus.ACTIVE);

        when(policyService.getPolicyById(5L)).thenReturn(res);

        mockMvc.perform(get("/api/v1/bonuses/policies/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void testGetAllPolicies() throws Exception {
        BonusPolicyResponse res = new BonusPolicyResponse();
        res.setId(5L);
        res.setName("Annual Performance Bonus 2026");

        when(policyService.getAllPolicies(any(), any()))
                .thenReturn(new PageImpl<>(List.of(res), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/bonuses/policies?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(5));
    }

    @Test
    void testActivatePolicy() throws Exception {
        BonusPolicyResponse res = new BonusPolicyResponse();
        res.setId(5L);
        res.setStatus(BonusPolicyStatus.ACTIVE);

        when(policyService.activatePolicy(5L)).thenReturn(res);

        mockMvc.perform(post("/api/v1/bonuses/policies/5/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void testDeactivatePolicy() throws Exception {
        BonusPolicyResponse res = new BonusPolicyResponse();
        res.setId(5L);
        res.setStatus(BonusPolicyStatus.INACTIVE);

        when(policyService.deactivatePolicy(5L)).thenReturn(res);

        mockMvc.perform(post("/api/v1/bonuses/policies/5/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    void testArchivePolicy() throws Exception {
        BonusPolicyResponse res = new BonusPolicyResponse();
        res.setId(5L);
        res.setStatus(BonusPolicyStatus.ARCHIVED);

        when(policyService.archivePolicy(5L)).thenReturn(res);

        mockMvc.perform(post("/api/v1/bonuses/policies/5/archive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ARCHIVED"));
    }
}
