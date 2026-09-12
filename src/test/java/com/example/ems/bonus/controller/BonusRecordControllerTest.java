package com.example.ems.bonus.controller;

import com.example.ems.bonus.dto.*;
import com.example.ems.bonus.entity.*;
import com.example.ems.bonus.repository.BonusRecordRepository;
import com.example.ems.bonus.service.BonusAdjustmentService;
import com.example.ems.bonus.service.BonusCalculationService;
import com.example.ems.bonus.service.BonusPayrollIntegrationService;
import com.example.ems.bonus.service.BonusWorkflowService;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class BonusRecordControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BonusCalculationService calculationService;

    @Mock
    private BonusAdjustmentService adjustmentService;

    @Mock
    private BonusWorkflowService workflowService;

    @Mock
    private BonusPayrollIntegrationService payrollIntegrationService;

    @Mock
    private BonusRecordRepository recordRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private BonusRecordController recordController;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final Long orgId = 1L;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);
        mockMvc = MockMvcBuilders.standaloneSetup(recordController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testPreviewBonusCalculation() throws Exception {
        BonusCalculateRequest req = new BonusCalculateRequest(101L, 5L, LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31));

        BonusPreviewResponse resp = new BonusPreviewResponse();
        resp.setEmployeeId(101L);
        resp.setPolicyId(5L);
        resp.setCalculatedAmount(BigDecimal.valueOf(15000.00));
        resp.setEligible(true);

        when(calculationService.previewBonus(any(BonusCalculateRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/bonuses/calculate/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeId").value(101))
                .andExpect(jsonPath("$.data.calculatedAmount").value(15000.00))
                .andExpect(jsonPath("$.data.eligible").value(true));
    }

    @Test
    void testCalculateAndPersistBonus() throws Exception {
        BonusCalculateRequest req = new BonusCalculateRequest(101L, 5L, LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31));

        BonusRecordResponse resp = new BonusRecordResponse();
        resp.setId(501L);
        resp.setEmployeeId(101L);
        resp.setPolicyId(5L);
        resp.setCalculatedAmount(BigDecimal.valueOf(15000.00));
        resp.setStatus(BonusStatus.CALCULATED);
        resp.setPayrollStatus(BonusPayrollStatus.PENDING);

        when(calculationService.calculateAndPersist(any(BonusCalculateRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/bonuses/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(501))
                .andExpect(jsonPath("$.data.status").value("CALCULATED"));
    }

    @Test
    void testAdjustBonus() throws Exception {
        BonusAdjustmentRequest req = new BonusAdjustmentRequest(
                BigDecimal.valueOf(12000.00),
                "Management-approved adjustment"
        );

        BonusRecordResponse resp = new BonusRecordResponse();
        resp.setId(501L);
        resp.setCalculatedAmount(BigDecimal.valueOf(15000.00));
        resp.setAdjustedAmount(BigDecimal.valueOf(12000.00));
        resp.setAdjustmentReason("Management-approved adjustment");
        resp.setStatus(BonusStatus.ADJUSTED);

        when(adjustmentService.adjustBonus(eq(501L), any(BonusAdjustmentRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/bonuses/501/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(501))
                .andExpect(jsonPath("$.data.calculatedAmount").value(15000.00))
                .andExpect(jsonPath("$.data.adjustedAmount").value(12000.00))
                .andExpect(jsonPath("$.data.status").value("ADJUSTED"));
    }

    @Test
    void testSubmitBonus() throws Exception {
        BonusRecordResponse resp = new BonusRecordResponse();
        resp.setId(501L);
        resp.setStatus(BonusStatus.APPROVED);
        resp.setApprovedAmount(BigDecimal.valueOf(12000.00));

        when(workflowService.submitForApproval(501L)).thenReturn(resp);

        mockMvc.perform(post("/api/v1/bonuses/501/submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(501))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    void testGetBonusById() throws Exception {
        BonusRecord rec = new BonusRecord();
        rec.setId(501L);
        rec.setCalculatedAmount(BigDecimal.valueOf(15000.00));
        rec.setStatus(BonusStatus.APPROVED);

        when(recordRepository.findByIdAndOrganizationId(501L, orgId)).thenReturn(Optional.of(rec));

        mockMvc.perform(get("/api/v1/bonuses/501"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(501));
    }

    @Test
    void testGetEligibleForPayroll() throws Exception {
        BonusRecordResponse resp = new BonusRecordResponse();
        resp.setId(501L);
        resp.setApprovedAmount(BigDecimal.valueOf(12000.00));

        when(payrollIntegrationService.getEligibleBonusRecords(eq(101L), any(), any()))
                .thenReturn(List.of(resp));

        mockMvc.perform(get("/api/v1/bonuses/payroll/eligible?employeeId=101&periodStart=2026-04-01&periodEnd=2027-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(501))
                .andExpect(jsonPath("$.data[0].approvedAmount").value(12000.00));
    }
}
