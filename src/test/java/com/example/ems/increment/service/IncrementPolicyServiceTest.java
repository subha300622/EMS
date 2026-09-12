package com.example.ems.increment.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.increment.dto.CreateIncrementPolicyRequest;
import com.example.ems.increment.dto.IncrementPolicyResponse;
import com.example.ems.increment.dto.PolicyBandDto;
import com.example.ems.increment.entity.EffectiveDateRule;
import com.example.ems.increment.entity.IncrementPolicy;
import com.example.ems.increment.repository.IncrementPolicyRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IncrementPolicyServiceTest {

    @Mock
    private IncrementPolicyRepository policyRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private IncrementPolicyService policyService;

    private Organization organization;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(1L);
        organization = new Organization();
        organization.setId(1L);
        organization.setName("Test Org");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testCreatePolicy_Success() {
        CreateIncrementPolicyRequest req = new CreateIncrementPolicyRequest();
        req.setName("FY 2026-27 Increment Policy");
        req.setMinimumRating(3.5);
        req.setMinimumGoalAchievementPercentage(70.0);
        req.setMinimumAttendancePercentage(90.0);
        req.setMaximumIncrementPercentage(15.0);
        req.setMinimumIncrementPercentage(3.0);
        req.setEffectiveDateRule(EffectiveDateRule.FIXED_DATE);
        req.setEffectiveDate(LocalDate.of(2026, 10, 1));
        req.setBudgetLimit(new BigDecimal("10000000"));
        req.setActive(true);

        List<PolicyBandDto> bands = new ArrayList<>();
        bands.add(new PolicyBandDto(4.5, 5.0, 15.0));
        bands.add(new PolicyBandDto(4.0, 4.49, 12.0));
        bands.add(new PolicyBandDto(3.5, 3.99, 10.0));
        req.setBands(bands);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(policyRepository.findMaxVersionByOrgId(1L)).thenReturn(1);
        when(policyRepository.findActivePoliciesByOrgId(1L)).thenReturn(new ArrayList<>());
        when(policyRepository.save(any(IncrementPolicy.class))).thenAnswer(inv -> {
            IncrementPolicy p = inv.getArgument(0);
            p.setId(101L);
            return p;
        });

        IncrementPolicyResponse resp = policyService.createPolicy(req);

        assertNotNull(resp);
        assertEquals(101L, resp.getId());
        assertEquals("FY 2026-27 Increment Policy", resp.getName());
        assertEquals(2, resp.getVersion());
        assertEquals(3.5, resp.getMinimumRating());
        assertEquals(15.0, resp.getMaximumIncrementPercentage());
        assertEquals(3.0, resp.getMinimumIncrementPercentage());
        assertTrue(resp.getActive());
        assertEquals(3, resp.getBands().size());
    }

    @Test
    void testCreatePolicy_ValidationFails_BlankName() {
        CreateIncrementPolicyRequest req = new CreateIncrementPolicyRequest();
        req.setName("   ");
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));

        assertThrows(BadRequestException.class, () -> policyService.createPolicy(req));
    }

    @Test
    void testCreatePolicy_ValidationFails_MissingFixedEffectiveDate() {
        CreateIncrementPolicyRequest req = new CreateIncrementPolicyRequest();
        req.setName("Test Policy");
        req.setEffectiveDateRule(EffectiveDateRule.FIXED_DATE);
        req.setEffectiveDate(null);
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));

        assertThrows(BadRequestException.class, () -> policyService.createPolicy(req));
    }

    @Test
    void testGetCurrentActivePolicy_NotFound() {
        when(policyRepository.findFirstActiveByOrgId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> policyService.getCurrentActivePolicy());
    }

    @Test
    void testGetCurrentActivePolicy_Found() {
        IncrementPolicy policy = new IncrementPolicy();
        policy.setId(10L);
        policy.setName("Active Policy");
        policy.setActive(true);
        policy.setVersion(1);

        when(policyRepository.findFirstActiveByOrgId(1L)).thenReturn(Optional.of(policy));

        IncrementPolicyResponse resp = policyService.getCurrentActivePolicy();
        assertNotNull(resp);
        assertEquals(10L, resp.getId());
        assertEquals("Active Policy", resp.getName());
    }
}
