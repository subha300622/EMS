package com.example.ems.bonus.service;

import com.example.ems.bonus.dto.BonusAdjustmentRequest;
import com.example.ems.bonus.dto.BonusRecordResponse;
import com.example.ems.bonus.entity.BonusRecord;
import com.example.ems.bonus.entity.BonusStatus;
import com.example.ems.bonus.repository.BonusRecordRepository;
import com.example.ems.common.exception.BadRequestException;
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
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BonusAdjustmentServiceTest {

    @Mock
    private BonusRecordRepository recordRepository;

    @InjectMocks
    private BonusAdjustmentService adjustmentService;

    private final Long orgId = 1L;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("hr.manager@acme.com", "pass", Collections.emptyList())
        );
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void testAdjustBonus_PreservesCalculatedAmount() {
        BonusRecord record = new BonusRecord();
        record.setId(501L);
        record.setCalculatedAmount(BigDecimal.valueOf(15000.00));
        record.setStatus(BonusStatus.CALCULATED);

        when(recordRepository.findByIdAndOrganizationId(501L, orgId)).thenReturn(Optional.of(record));
        when(recordRepository.save(any(BonusRecord.class))).thenAnswer(i -> i.getArgument(0));

        BonusAdjustmentRequest req = new BonusAdjustmentRequest(
                BigDecimal.valueOf(12000.00),
                "Management-approved adjustment due to exceptional contribution"
        );

        BonusRecordResponse resp = adjustmentService.adjustBonus(501L, req);

        assertNotNull(resp);
        assertEquals(BigDecimal.valueOf(15000.00), resp.getCalculatedAmount()); // Original preserved
        assertEquals(BigDecimal.valueOf(12000.00), resp.getAdjustedAmount());   // Adjusted set
        assertEquals(BigDecimal.valueOf(12000.00), resp.getEffectiveAmount());  // Effective is adjusted
        assertEquals(BonusStatus.ADJUSTED, resp.getStatus());
        assertEquals("Management-approved adjustment due to exceptional contribution", resp.getAdjustmentReason());
        assertEquals("hr.manager@acme.com", resp.getAdjustedBy());
        assertNotNull(resp.getAdjustedAt());
    }

    @Test
    void testAdjustBonus_FailsIfAlreadyApproved() {
        BonusRecord record = new BonusRecord();
        record.setId(501L);
        record.setStatus(BonusStatus.APPROVED);

        when(recordRepository.findByIdAndOrganizationId(501L, orgId)).thenReturn(Optional.of(record));

        BonusAdjustmentRequest req = new BonusAdjustmentRequest(BigDecimal.valueOf(10000.00), "Adjustment");
        assertThrows(BadRequestException.class, () -> adjustmentService.adjustBonus(501L, req));
    }
}
