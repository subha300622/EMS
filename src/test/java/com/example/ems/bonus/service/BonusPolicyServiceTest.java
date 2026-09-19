package com.example.ems.bonus.service;

import com.example.ems.bonus.dto.BonusPolicyRequest;
import com.example.ems.bonus.dto.BonusPolicyResponse;
import com.example.ems.bonus.entity.BonusCalculationMethod;
import com.example.ems.bonus.entity.BonusPolicy;
import com.example.ems.bonus.entity.BonusPolicyStatus;
import com.example.ems.bonus.entity.BonusType;
import com.example.ems.bonus.repository.BonusPolicyRepository;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.employee.entity.Employee;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BonusPolicyServiceTest {

    @Mock
    private BonusPolicyRepository policyRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private BonusPolicyService policyService;

    private Organization organization;
    private final Long orgId = 1L;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);
        organization = new Organization();
        organization.setId(orgId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testCreatePolicy_Success() {
        BonusPolicyRequest req = new BonusPolicyRequest();
        req.setName("Annual Performance Bonus 2026");
        req.setBonusType(BonusType.PERFORMANCE);
        req.setCalculationMethod(BonusCalculationMethod.FIXED_AMOUNT);
        req.setFixedAmount(BigDecimal.valueOf(15000.00));
        req.setEffectiveFrom(LocalDate.of(2026, 4, 1));
        req.setEffectiveTo(LocalDate.of(2027, 3, 31));

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(policyRepository.existsByOrganizationIdAndNameIgnoreCase(eq(orgId), any())).thenReturn(false);
        when(policyRepository.save(any(BonusPolicy.class))).thenAnswer(i -> {
            BonusPolicy p = i.getArgument(0);
            p.setId(100L);
            return p;
        });

        BonusPolicyResponse resp = policyService.createPolicy(req);

        assertNotNull(resp);
        assertEquals(100L, resp.getId());
        assertEquals("Annual Performance Bonus 2026", resp.getName());
        assertEquals(BonusPolicyStatus.DRAFT, resp.getStatus());
        assertEquals(1L, resp.getPolicyVersion());
    }

    @Test
    void testPolicyLifecycleTransitions() {
        BonusPolicy policy = new BonusPolicy();
        policy.setId(100L);
        policy.setName("Festival Bonus");
        policy.setStatus(BonusPolicyStatus.DRAFT);
        policy.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        policy.setCalculationMethod(BonusCalculationMethod.FIXED_AMOUNT);
        policy.setFixedAmount(BigDecimal.valueOf(5000.00));

        when(policyRepository.findByIdAndOrganizationId(100L, orgId)).thenReturn(Optional.of(policy));
        when(policyRepository.save(any(BonusPolicy.class))).thenAnswer(i -> i.getArgument(0));

        // DRAFT -> ACTIVE
        BonusPolicyResponse active = policyService.activatePolicy(100L);
        assertEquals(BonusPolicyStatus.ACTIVE, active.getStatus());
        assertEquals(2L, active.getPolicyVersion());

        // ACTIVE -> INACTIVE
        BonusPolicyResponse inactive = policyService.deactivatePolicy(100L);
        assertEquals(BonusPolicyStatus.INACTIVE, inactive.getStatus());

        // INACTIVE -> ARCHIVED
        BonusPolicyResponse archived = policyService.archivePolicy(100L);
        assertEquals(BonusPolicyStatus.ARCHIVED, archived.getStatus());
    }

    @Test
    void testUpdatePolicy_FailsWhenActive() {
        BonusPolicy activePolicy = new BonusPolicy();
        activePolicy.setId(100L);
        activePolicy.setStatus(BonusPolicyStatus.ACTIVE);

        when(policyRepository.findByIdAndOrganizationId(100L, orgId)).thenReturn(Optional.of(activePolicy));

        BonusPolicyRequest updateReq = new BonusPolicyRequest();
        updateReq.setName("Updated Bonus");

        assertThrows(BadRequestException.class, () -> policyService.updatePolicy(100L, updateReq));
    }

    @Test
    void testResolveApplicablePolicy() {
        Employee emp = new Employee();
        emp.setId(10L);
        emp.setDepartment("Engineering");
        emp.setLocation("HQ");
        emp.setEmploymentType("FULL_TIME");

        BonusPolicy matching = new BonusPolicy();
        matching.setId(200L);
        matching.setDepartmentId("Engineering");
        matching.setBranchId("HQ");
        matching.setStatus(BonusPolicyStatus.ACTIVE);

        when(policyRepository.findActivePoliciesForDate(eq(orgId), any())).thenReturn(List.of(matching));

        Optional<BonusPolicy> resolved = policyService.resolveApplicablePolicy(emp, LocalDate.now());
        assertTrue(resolved.isPresent());
        assertEquals(200L, resolved.get().getId());
    }
}
