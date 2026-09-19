package com.example.ems.incentive.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.incentive.dto.IncentiveAdjustmentRequest;
import com.example.ems.incentive.dto.IncentiveRecordResponse;
import com.example.ems.incentive.entity.*;
import com.example.ems.incentive.repository.IncentiveRecordRepository;
import com.example.ems.organization.entity.Organization;
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
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IncentiveAdjustmentServiceTest {

    @Mock
    private IncentiveRecordRepository recordRepository;

    @InjectMocks
    private IncentiveAdjustmentService adjustmentService;

    private IncentiveRecord record;
    private IncentivePolicy policy;
    private final Long orgId = 1L;
    private final Long recordId = 100L;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("manager@company.com", "password", Collections.emptyList()));

        Organization org = new Organization();
        org.setId(orgId);

        Employee emp = new Employee();
        emp.setId(10L);

        policy = new IncentivePolicy();
        policy.setId(1L);
        policy.setMaximumAmount(BigDecimal.valueOf(10000.00));

        record = new IncentiveRecord();
        record.setId(recordId);
        record.setOrganization(org);
        record.setEmployee(emp);
        record.setPolicy(policy);
        record.setCalculatedAmount(BigDecimal.valueOf(8000.00));
        record.setStatus(IncentiveStatus.CALCULATED);
        record.setPayrollStatus(IncentivePayrollStatus.PENDING);
        record.setPeriodStart(LocalDate.of(2026, 9, 1));
        record.setPeriodEnd(LocalDate.of(2026, 9, 30));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void testAdjustmentSucceedsAndPreservesOriginalCalculation() {
        IncentiveAdjustmentRequest req = new IncentiveAdjustmentRequest(BigDecimal.valueOf(5000.00), "Capped after review");

        when(recordRepository.findByIdAndOrganizationId(recordId, orgId)).thenReturn(Optional.of(record));
        when(recordRepository.save(any(IncentiveRecord.class))).thenAnswer(i -> i.getArgument(0));

        IncentiveRecordResponse resp = adjustmentService.adjustIncentive(recordId, req);

        assertNotNull(resp);
        assertEquals(0, new BigDecimal("8000.00").compareTo(resp.getCalculatedAmount())); // Original preserved!
        assertEquals(0, new BigDecimal("5000.00").compareTo(resp.getAdjustedAmount()));
        assertEquals(0, new BigDecimal("5000.00").compareTo(resp.getEffectiveAmount()));
        assertEquals("Capped after review", resp.getAdjustmentReason());
        assertEquals("manager@company.com", resp.getAdjustedBy());
        assertEquals(IncentiveStatus.ADJUSTED, resp.getStatus());
        assertNotNull(resp.getAdjustedAt());
    }

    @Test
    void testNegativeAdjustmentRejected() {
        IncentiveAdjustmentRequest req = new IncentiveAdjustmentRequest(BigDecimal.valueOf(-500.00), "Invalid negative");

        when(recordRepository.findByIdAndOrganizationId(recordId, orgId)).thenReturn(Optional.of(record));

        assertThrows(BadRequestException.class, () -> adjustmentService.adjustIncentive(recordId, req));
    }

    @Test
    void testAboveCapAdjustmentRejected() {
        IncentiveAdjustmentRequest req = new IncentiveAdjustmentRequest(BigDecimal.valueOf(15000.00), "Exceeds max 10000");

        when(recordRepository.findByIdAndOrganizationId(recordId, orgId)).thenReturn(Optional.of(record));

        assertThrows(BadRequestException.class, () -> adjustmentService.adjustIncentive(recordId, req));
    }

    @Test
    void testMissingReasonRejected() {
        IncentiveAdjustmentRequest req = new IncentiveAdjustmentRequest(BigDecimal.valueOf(5000.00), "   ");

        when(recordRepository.findByIdAndOrganizationId(recordId, orgId)).thenReturn(Optional.of(record));

        assertThrows(BadRequestException.class, () -> adjustmentService.adjustIncentive(recordId, req));
    }

    @Test
    void testAdjustmentAfterApprovedRejected() {
        record.setStatus(IncentiveStatus.APPROVED);
        IncentiveAdjustmentRequest req = new IncentiveAdjustmentRequest(BigDecimal.valueOf(5000.00), "Valid reason");

        when(recordRepository.findByIdAndOrganizationId(recordId, orgId)).thenReturn(Optional.of(record));

        assertThrows(BadRequestException.class, () -> adjustmentService.adjustIncentive(recordId, req));
    }

    @Test
    void testAdjustmentAfterPostedToPayrollRejected() {
        record.setStatus(IncentiveStatus.POSTED_TO_PAYROLL);
        IncentiveAdjustmentRequest req = new IncentiveAdjustmentRequest(BigDecimal.valueOf(5000.00), "Valid reason");

        when(recordRepository.findByIdAndOrganizationId(recordId, orgId)).thenReturn(Optional.of(record));

        assertThrows(BadRequestException.class, () -> adjustmentService.adjustIncentive(recordId, req));
    }
}
