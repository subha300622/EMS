package com.example.ems.overtime.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.overtime.dto.OvertimeAdjustmentRequest;
import com.example.ems.overtime.dto.OvertimeRecordResponse;
import com.example.ems.overtime.entity.*;
import com.example.ems.overtime.repository.OvertimeRecordRepository;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OvertimeAdjustmentServiceTest {

    @Mock
    private OvertimeRecordRepository recordRepository;

    @InjectMocks
    private OvertimeAdjustmentService adjustmentService;

    private OvertimeRecord record;
    private OvertimePolicy policy;
    private final Long orgId = 1L;
    private final Long recordId = 100L;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("manager@company.com", "pass")
        );

        policy = new OvertimePolicy();
        policy.setMaximumOtMinutes(240); // 4 hrs cap

        Employee employee = new Employee();
        employee.setId(10L);
        employee.setFullName("Jane Smith");

        record = new OvertimeRecord();
        record.setId(recordId);
        record.setEmployee(employee);
        record.setPolicy(policy);
        record.setWorkDate(LocalDate.of(2026, 9, 9));
        record.setScheduledMinutes(480);
        record.setWorkedMinutes(660); // 3 hrs OT
        record.setCalculatedOtMinutes(180);
        record.setHourlyRate(new BigDecimal("144.23"));
        record.setOtMultiplier(BigDecimal.valueOf(1.50));
        record.setOtRate(new BigDecimal("216.35"));
        record.setCalculatedAmount(new BigDecimal("649.05"));
        record.setStatus(OvertimeStatus.CALCULATED);
        record.setPayrollStatus(OvertimePayrollStatus.PENDING);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void testAdjustmentSucceeds_PreservesOriginalCalculations() {
        when(recordRepository.findByIdAndOrganizationId(recordId, orgId)).thenReturn(Optional.of(record));
        when(recordRepository.save(any(OvertimeRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        OvertimeAdjustmentRequest req = new OvertimeAdjustmentRequest(120, "Manager authorized only 2 hours");
        OvertimeRecordResponse resp = adjustmentService.adjustOvertime(recordId, req);

        assertNotNull(resp);
        // Invariant: original calculated values MUST remain unchanged
        assertEquals(180, resp.getCalculatedOtMinutes());
        assertEquals(new BigDecimal("649.05"), resp.getCalculatedAmount());

        // Adjusted values saved
        assertEquals(120, resp.getAdjustedOtMinutes());
        // 2 hrs * 216.35 = 432.70
        assertEquals(new BigDecimal("432.70"), resp.getAdjustedAmount());
        assertEquals(120, resp.getEffectiveOtMinutes());
        assertEquals(new BigDecimal("432.70"), resp.getEffectiveAmount());

        assertEquals(OvertimeStatus.ADJUSTED, resp.getStatus());
        assertEquals("Manager authorized only 2 hours", resp.getAdjustmentReason());
        assertEquals("manager@company.com", resp.getAdjustedBy());
        assertNotNull(resp.getAdjustedAt());

        verify(recordRepository, times(1)).save(record);
    }

    @Test
    void testNegativeAdjustment_ThrowsBadRequest() {
        OvertimeAdjustmentRequest req = new OvertimeAdjustmentRequest(-10, "Invalid negative");
        assertThrows(BadRequestException.class, () -> adjustmentService.adjustOvertime(recordId, req));
    }

    @Test
    void testAboveCapAdjustment_ThrowsBadRequest() {
        when(recordRepository.findByIdAndOrganizationId(recordId, orgId)).thenReturn(Optional.of(record));

        OvertimeAdjustmentRequest req = new OvertimeAdjustmentRequest(300, "Exceeds 240 min policy cap");
        assertThrows(BadRequestException.class, () -> adjustmentService.adjustOvertime(recordId, req));
    }

    @Test
    void testMissingReason_ThrowsBadRequest() {
        OvertimeAdjustmentRequest req = new OvertimeAdjustmentRequest(60, "   ");
        assertThrows(BadRequestException.class, () -> adjustmentService.adjustOvertime(recordId, req));
    }

    @Test
    void testAdjustmentAfterApproved_ThrowsBadRequest() {
        record.setStatus(OvertimeStatus.APPROVED);
        when(recordRepository.findByIdAndOrganizationId(recordId, orgId)).thenReturn(Optional.of(record));

        OvertimeAdjustmentRequest req = new OvertimeAdjustmentRequest(60, "Attempt adjust approved");
        assertThrows(BadRequestException.class, () -> adjustmentService.adjustOvertime(recordId, req));
    }

    @Test
    void testAdjustmentAfterPostedToPayroll_ThrowsBadRequest() {
        record.setStatus(OvertimeStatus.POSTED_TO_PAYROLL);
        when(recordRepository.findByIdAndOrganizationId(recordId, orgId)).thenReturn(Optional.of(record));

        OvertimeAdjustmentRequest req = new OvertimeAdjustmentRequest(60, "Attempt adjust posted");
        assertThrows(BadRequestException.class, () -> adjustmentService.adjustOvertime(recordId, req));
    }

    @Test
    void testAdjustmentRecordNotFound_ThrowsResourceNotFound() {
        when(recordRepository.findByIdAndOrganizationId(recordId, orgId)).thenReturn(Optional.empty());

        OvertimeAdjustmentRequest req = new OvertimeAdjustmentRequest(60, "Valid reason");
        assertThrows(ResourceNotFoundException.class, () -> adjustmentService.adjustOvertime(recordId, req));
    }
}
