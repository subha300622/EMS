package com.example.ems.incentive.controller;

import com.example.ems.incentive.dto.*;
import com.example.ems.incentive.entity.IncentiveCalculationMethod;
import com.example.ems.incentive.entity.IncentivePayrollStatus;
import com.example.ems.incentive.entity.IncentiveRecord;
import com.example.ems.incentive.entity.IncentiveStatus;
import com.example.ems.incentive.repository.IncentiveRecordRepository;
import com.example.ems.incentive.service.IncentiveAdjustmentService;
import com.example.ems.incentive.service.IncentiveCalculationService;
import com.example.ems.incentive.service.IncentivePayrollIntegrationService;
import com.example.ems.incentive.service.IncentiveWorkflowService;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class IncentiveRecordControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IncentiveCalculationService calculationService;

    @Mock
    private IncentiveAdjustmentService adjustmentService;

    @Mock
    private IncentiveWorkflowService workflowService;

    @Mock
    private IncentivePayrollIntegrationService payrollIntegrationService;

    @Mock
    private IncentiveRecordRepository recordRepository;

    @InjectMocks
    private IncentiveRecordController recordController;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(1L);
        mockMvc = MockMvcBuilders.standaloneSetup(recordController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testPreviewCalculation() throws Exception {
        IncentiveCalculateRequest req = new IncentiveCalculateRequest();
        req.setEmployeeId(100L);
        req.setPeriodStart(LocalDate.of(2026, 3, 1));
        req.setPeriodEnd(LocalDate.of(2026, 3, 31));

        IncentivePreviewResponse res = IncentivePreviewResponse.builder()
                .employeeId(100L)
                .eligible(true)
                .calculatedAmount(new BigDecimal("5000.00"))
                .calculationMethod(IncentiveCalculationMethod.FIXED_AMOUNT)
                .build();

        when(calculationService.previewIncentive(any(IncentiveCalculateRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/incentives/calculate/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeId").value(100))
                .andExpect(jsonPath("$.data.eligible").value(true))
                .andExpect(jsonPath("$.data.calculatedAmount").value(5000.00));
    }

    @Test
    void testCalculateAndPersist() throws Exception {
        IncentiveCalculateRequest req = new IncentiveCalculateRequest();
        req.setEmployeeId(100L);
        req.setPeriodStart(LocalDate.of(2026, 3, 1));
        req.setPeriodEnd(LocalDate.of(2026, 3, 31));

        IncentiveRecordResponse res = IncentiveRecordResponse.builder()
                .id(200L)
                .employeeId(100L)
                .calculatedAmount(new BigDecimal("5000.00"))
                .status(IncentiveStatus.CALCULATED)
                .payrollStatus(IncentivePayrollStatus.PENDING)
                .build();

        when(calculationService.calculateAndPersist(any(IncentiveCalculateRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/incentives/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(200))
                .andExpect(jsonPath("$.data.status").value("CALCULATED"));
    }

    @Test
    void testAdjustIncentive() throws Exception {
        IncentiveAdjustmentRequest req = new IncentiveAdjustmentRequest();
        req.setAdjustedAmount(new BigDecimal("4500.00"));
        req.setAdjustmentReason("Manager review adjustment");

        IncentiveRecordResponse res = IncentiveRecordResponse.builder()
                .id(200L)
                .employeeId(100L)
                .calculatedAmount(new BigDecimal("5000.00"))
                .adjustedAmount(new BigDecimal("4500.00"))
                .adjustmentReason("Manager review adjustment")
                .status(IncentiveStatus.ADJUSTED)
                .build();

        when(adjustmentService.adjustIncentive(eq(200L), any(IncentiveAdjustmentRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/incentives/200/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.adjustedAmount").value(4500.00))
                .andExpect(jsonPath("$.data.status").value("ADJUSTED"));
    }

    @Test
    void testSubmitForApproval() throws Exception {
        IncentiveRecordResponse res = IncentiveRecordResponse.builder()
                .id(200L)
                .status(IncentiveStatus.PENDING_APPROVAL)
                .build();

        when(workflowService.submitForApproval(200L)).thenReturn(res);

        mockMvc.perform(post("/api/v1/incentives/200/submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING_APPROVAL"));
    }

    @Test
    void testCancelIncentive() throws Exception {
        IncentiveRecordResponse res = IncentiveRecordResponse.builder()
                .id(200L)
                .status(IncentiveStatus.CANCELLED)
                .build();

        when(workflowService.cancelIncentive(200L, "Cancelled by requester")).thenReturn(res);

        mockMvc.perform(post("/api/v1/incentives/200/cancel")
                        .param("reason", "Cancelled by requester"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    void testGetRecordById() throws Exception {
        IncentiveRecord record = IncentiveRecord.builder()
                .id(200L)
                .calculatedAmount(new BigDecimal("5000.00"))
                .status(IncentiveStatus.CALCULATED)
                .build();

        when(recordRepository.findByIdAndOrganizationId(200L, 1L)).thenReturn(Optional.of(record));

        mockMvc.perform(get("/api/v1/incentives/200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(200));
    }

    @Test
    void testSearchRecords() throws Exception {
        IncentiveRecord record = IncentiveRecord.builder()
                .id(200L)
                .calculatedAmount(new BigDecimal("5000.00"))
                .build();

        when(recordRepository.findFiltered(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(record), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/incentives?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(200));
    }

    @Test
    void testGetEligibleForPayroll() throws Exception {
        IncentiveRecordResponse res = IncentiveRecordResponse.builder()
                .id(200L)
                .employeeId(100L)
                .status(IncentiveStatus.APPROVED)
                .payrollStatus(IncentivePayrollStatus.PENDING)
                .build();

        when(payrollIntegrationService.getEligibleIncentiveRecords(eq(100L), any(), any()))
                .thenReturn(List.of(res));

        mockMvc.perform(get("/api/v1/incentives/payroll/eligible")
                        .param("employeeId", "100")
                        .param("periodStart", "2026-03-01")
                        .param("periodEnd", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(200));
    }
}
