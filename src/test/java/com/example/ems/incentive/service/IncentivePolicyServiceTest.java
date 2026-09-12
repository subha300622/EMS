package com.example.ems.incentive.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.incentive.dto.IncentivePolicyRequest;
import com.example.ems.incentive.dto.IncentivePolicyResponse;
import com.example.ems.incentive.entity.IncentiveCalculationMethod;
import com.example.ems.incentive.entity.IncentivePolicy;
import com.example.ems.incentive.entity.IncentivePolicyStatus;
import com.example.ems.incentive.entity.IncentiveType;
import com.example.ems.incentive.repository.IncentivePolicyRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IncentivePolicyServiceTest {

    @Mock
    private IncentivePolicyRepository policyRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private IncentivePolicyService policyService;

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
    void testCreatePolicy() {
        IncentivePolicyRequest req = new IncentivePolicyRequest();
        req.setName("Tech Sales Incentive");
        req.setIncentiveType(IncentiveType.SALES);
        req.setCalculationMethod(IncentiveCalculationMethod.FIXED_AMOUNT);
        req.setFixedAmount(BigDecimal.valueOf(5000.00));
        req.setEffectiveFrom(LocalDate.of(2026, 1, 1));

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(policyRepository.existsByOrganizationIdAndNameIgnoreCase(eq(orgId), any())).thenReturn(false);
        when(policyRepository.save(any(IncentivePolicy.class))).thenAnswer(i -> {
            IncentivePolicy p = i.getArgument(0);
            p.setId(10L);
            return p;
        });

        IncentivePolicyResponse resp = policyService.createPolicy(req);

        assertNotNull(resp);
        assertEquals(10L, resp.getId());
        assertEquals("Tech Sales Incentive", resp.getName());
        assertEquals(IncentivePolicyStatus.DRAFT, resp.getStatus());
    }

    @Test
    void testPolicyLifecycleTransitions() {
        IncentivePolicy policy = new IncentivePolicy();
        policy.setId(10L);
        policy.setStatus(IncentivePolicyStatus.DRAFT);

        when(policyRepository.findByIdAndOrganizationId(10L, orgId)).thenReturn(Optional.of(policy));
        when(policyRepository.save(any(IncentivePolicy.class))).thenAnswer(i -> i.getArgument(0));

        // DRAFT -> ACTIVE
        IncentivePolicyResponse active = policyService.activatePolicy(10L);
        assertEquals(IncentivePolicyStatus.ACTIVE, active.getStatus());

        // ACTIVE -> INACTIVE
        IncentivePolicyResponse inactive = policyService.deactivatePolicy(10L);
        assertEquals(IncentivePolicyStatus.INACTIVE, inactive.getStatus());

        // INACTIVE -> ARCHIVED
        IncentivePolicyResponse archived = policyService.archivePolicy(10L);
        assertEquals(IncentivePolicyStatus.ARCHIVED, archived.getStatus());
    }

    @Test
    void testInvalidLifecycleTransitionThrows() {
        IncentivePolicy policy = new IncentivePolicy();
        policy.setId(10L);
        policy.setStatus(IncentivePolicyStatus.ARCHIVED);

        when(policyRepository.findByIdAndOrganizationId(10L, orgId)).thenReturn(Optional.of(policy));

        assertThrows(BadRequestException.class, () -> policyService.activatePolicy(10L));
    }

    @Test
    void testDeterministicApplicabilityPrecedence() {
        Employee emp = new Employee();
        emp.setId(100L);
        emp.setDepartment("Engineering");
        emp.setDesignation("Software Engineer");
        emp.setEmploymentType("FULL_TIME");

        // Policy 1: Org fallback (score 0)
        IncentivePolicy fallbackPol = new IncentivePolicy();
        fallbackPol.setId(1L);
        fallbackPol.setStatus(IncentivePolicyStatus.ACTIVE);

        // Policy 2: Dept match (score 8)
        IncentivePolicy deptPol = new IncentivePolicy();
        deptPol.setId(2L);
        deptPol.setDepartmentId("Engineering");
        deptPol.setStatus(IncentivePolicyStatus.ACTIVE);

        // Policy 3: Dept + Designation match (score 12)
        IncentivePolicy specificPol = new IncentivePolicy();
        specificPol.setId(3L);
        specificPol.setDepartmentId("Engineering");
        specificPol.setDesignationId("Software Engineer");
        specificPol.setStatus(IncentivePolicyStatus.ACTIVE);

        when(policyRepository.findActivePoliciesForDate(eq(orgId), any(LocalDate.class)))
                .thenReturn(List.of(fallbackPol, deptPol, specificPol));

        Optional<IncentivePolicy> resolved = policyService.resolveApplicablePolicy(emp, LocalDate.of(2026, 9, 1));

        assertTrue(resolved.isPresent());
        assertEquals(3L, resolved.get().getId()); // Highest score selected
    }
}
