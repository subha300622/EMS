package com.example.ems.onboarding.controller;

import com.example.ems.onboarding.dto.policy.OnboardingApprovalPolicyRequest;
import com.example.ems.onboarding.entity.OnboardingApprovalPolicy;
import com.example.ems.onboarding.service.OnboardingApprovalPolicyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class OnboardingApprovalPolicyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OnboardingApprovalPolicyService policyService;

    @InjectMocks
    private OnboardingApprovalPolicyController policyController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(policyController).build();
    }

    @Test
    @DisplayName("GET /api/v1/onboarding/policies/status-transitions returns system status transition matrix")
    public void testGetStatusTransitionPolicies() throws Exception {
        mockMvc.perform(get("/api/v1/onboarding/policies/status-transitions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Status transition policies retrieved successfully"));
    }

    @Test
    @DisplayName("GET /api/v1/onboarding/approval-policies returns tenant active policies")
    public void testGetApprovalPolicies() throws Exception {
        OnboardingApprovalPolicy policy = new OnboardingApprovalPolicy();
        policy.setId(1L);
        policy.setPolicyId("ONB-POL-001");
        policy.setCurrentStatus("COMPLETED");
        policy.setAction("APPROVE");
        policy.setNextStatus("APPROVED");

        when(policyService.getApprovalPoliciesForCurrentTenant()).thenReturn(List.of(policy));

        mockMvc.perform(get("/api/v1/onboarding/approval-policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].policyId").value("ONB-POL-001"));
    }

    @Test
    @DisplayName("POST /api/v1/onboarding/approval-policies creates new organization policy")
    public void testCreateApprovalPolicy() throws Exception {
        OnboardingApprovalPolicyRequest request = new OnboardingApprovalPolicyRequest();
        request.setCurrentStatus("COMPLETED");
        request.setAction("APPROVE");
        request.setNextStatus("APPROVED");
        request.setApprover(Map.of("type", "CONFIGURED_ROLE", "roleId", 42));

        OnboardingApprovalPolicy created = new OnboardingApprovalPolicy();
        created.setId(10L);
        created.setPolicyId("ONB-POL-CUSTOM");
        created.setCurrentStatus("COMPLETED");
        created.setAction("APPROVE");
        created.setNextStatus("APPROVED");

        when(policyService.createApprovalPolicy(any())).thenReturn(created);

        mockMvc.perform(post("/api/v1/onboarding/approval-policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.policyId").value("ONB-POL-CUSTOM"));
    }

    @Test
    @DisplayName("POST /api/v1/onboarding/{onboardingId}/approve executes policy-evaluated approval")
    public void testApproveOnboarding() throws Exception {
        when(policyService.approveOnboardingWithPolicy(eq(42L), any()))
                .thenReturn(Map.of("onboardingId", 42L, "status", "APPROVED"));

        mockMvc.perform(post("/api/v1/onboarding/42/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("remarks", "Approved by policy"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }
}
