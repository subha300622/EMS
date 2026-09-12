package com.example.ems.overtime.controller;

import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.overtime.dto.*;
import com.example.ems.overtime.entity.*;
import com.example.ems.overtime.repository.OvertimeRecordRepository;
import com.example.ems.overtime.service.OvertimeAdjustmentService;
import com.example.ems.overtime.service.OvertimeCalculationService;
import com.example.ems.overtime.service.OvertimeWorkflowService;
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

import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

@ExtendWith(MockitoExtension.class)
public class OvertimeRecordControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OvertimeCalculationService calculationService;

    @Mock
    private OvertimeAdjustmentService adjustmentService;

    @Mock
    private OvertimeWorkflowService workflowService;

    @Mock
    private OvertimeRecordRepository recordRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private OvertimeRecordController recordController;

    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

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
    void testPreviewOvertime() throws Exception {
        OvertimeCalculateRequest req = new OvertimeCalculateRequest();
        req.setAttendanceId(1250L);

        OvertimePreviewResponse res = new OvertimePreviewResponse();
        res.setAttendanceId(1250L);
        res.setEmployeeId(101L);
        res.setWorkDate(LocalDate.of(2026, 9, 1));
        res.setScheduledMinutes(480);
        res.setWorkedMinutes(600);
        res.setRawOtMinutes(120);
        res.setCalculatedOtMinutes(120);
        res.setDayType(OvertimeDayType.NORMAL_DAY);
        res.setAmountBasis(OvertimeAmountBasis.BASIC_SALARY);
        res.setHourlyRate(BigDecimal.valueOf(100.00));
        res.setOtMultiplier(BigDecimal.valueOf(1.50));
        res.setOtRate(BigDecimal.valueOf(150.00));
        res.setCalculatedAmount(BigDecimal.valueOf(300.00));
        res.setPolicyId(1L);
        res.setPolicyVersion(1L);

        when(calculationService.previewOvertime(eq(1250L))).thenReturn(res);

        mockMvc.perform(post("/api/v1/overtime/calculate/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.attendanceId").value(1250))
                .andExpect(jsonPath("$.data.calculatedOtMinutes").value(120))
                .andExpect(jsonPath("$.data.calculatedAmount").value(300.00));
    }

    @Test
    void testCalculateAndPersistOvertime() throws Exception {
        OvertimeCalculateRequest req = new OvertimeCalculateRequest();
        req.setAttendanceId(1250L);

        OvertimeRecordResponse res = new OvertimeRecordResponse();
        res.setId(50L);
        res.setEmployeeId(101L);
        res.setAttendanceId(1250L);
        res.setWorkDate(LocalDate.of(2026, 9, 1));
        res.setScheduledMinutes(480);
        res.setWorkedMinutes(600);
        res.setCalculatedOtMinutes(120);
        res.setCalculatedAmount(BigDecimal.valueOf(300.00));
        res.setStatus(OvertimeStatus.CALCULATED);
        res.setPayrollStatus(OvertimePayrollStatus.PENDING);

        when(calculationService.calculateOvertime(eq(1250L))).thenReturn(res);

        mockMvc.perform(post("/api/v1/overtime/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(50))
                .andExpect(jsonPath("$.data.status").value("CALCULATED"));
    }

    @Test
    void testAdjustOvertime() throws Exception {
        OvertimeAdjustmentRequest req = new OvertimeAdjustmentRequest();
        req.setAdjustedOtMinutes(90);
        req.setReason("Manager approved only 1.5h");

        OvertimeRecordResponse res = new OvertimeRecordResponse();
        res.setId(50L);
        res.setCalculatedOtMinutes(120);
        res.setCalculatedAmount(BigDecimal.valueOf(300.00));
        res.setAdjustedOtMinutes(90);
        res.setAdjustedAmount(BigDecimal.valueOf(225.00));
        res.setAdjustmentReason("Manager approved only 1.5h");
        res.setStatus(OvertimeStatus.ADJUSTED);

        when(adjustmentService.adjustOvertime(eq(50L), any(OvertimeAdjustmentRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/overtime/50/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.adjustedOtMinutes").value(90))
                .andExpect(jsonPath("$.data.adjustedAmount").value(225.00))
                .andExpect(jsonPath("$.data.status").value("ADJUSTED"));
    }

    @Test
    void testSubmitOvertime() throws Exception {
        OvertimeRecordResponse res = new OvertimeRecordResponse();
        res.setId(50L);
        res.setStatus(OvertimeStatus.PENDING_APPROVAL);
        res.setWorkflowInstanceId("999");

        when(workflowService.submitOvertime(50L)).thenReturn(res);

        mockMvc.perform(post("/api/v1/overtime/50/submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(50))
                .andExpect(jsonPath("$.data.status").value("PENDING_APPROVAL"))
                .andExpect(jsonPath("$.data.workflowInstanceId").value("999"));
    }

    @Test
    void testGetRecordById() throws Exception {
        OvertimeRecord entity = new OvertimeRecord();
        entity.setId(50L);
        entity.setWorkDate(LocalDate.of(2026, 9, 1));
        entity.setScheduledMinutes(480);
        entity.setWorkedMinutes(600);
        entity.setCalculatedOtMinutes(120);
        entity.setCalculatedAmount(BigDecimal.valueOf(300.00));
        entity.setStatus(OvertimeStatus.APPROVED);
        entity.setPayrollStatus(OvertimePayrollStatus.PENDING);

        when(recordRepository.findByIdAndOrganizationId(50L, 1L)).thenReturn(Optional.of(entity));

        mockMvc.perform(get("/api/v1/overtime/50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(50));
    }

    @Test
    void testGetRecords() throws Exception {
        OvertimeRecord entity = new OvertimeRecord();
        entity.setId(50L);
        entity.setWorkDate(LocalDate.of(2026, 9, 1));
        entity.setScheduledMinutes(480);
        entity.setWorkedMinutes(600);
        entity.setCalculatedOtMinutes(120);
        entity.setCalculatedAmount(BigDecimal.valueOf(300.00));
        entity.setStatus(OvertimeStatus.APPROVED);
        entity.setPayrollStatus(OvertimePayrollStatus.PENDING);

        when(recordRepository.findFiltered(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/overtime?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(50));
    }
}
